package com.merge.final_project.redemption.service;

import com.merge.final_project.blockchain.entity.Transaction;
import com.merge.final_project.blockchain.entity.TransactionEventType;
import com.merge.final_project.blockchain.entity.TransactionStatus;
import com.merge.final_project.blockchain.repository.TransactionRepository;
import com.merge.final_project.blockchain.service.BlockchainService;
import com.merge.final_project.org.Foundation;
import com.merge.final_project.org.FoundationRepository;
import com.merge.final_project.recipient.beneficiary.entity.Beneficiary;
import com.merge.final_project.recipient.beneficiary.repository.BeneficiaryRepository;
import com.merge.final_project.redemption.RedemptionStatus;
import com.merge.final_project.redemption.RequesterType;
import com.merge.final_project.redemption.dto.request.RedemptionRequest;
import com.merge.final_project.redemption.dto.response.RedemptionDetailResponse;
import com.merge.final_project.redemption.dto.response.RedemptionListResponse;
import com.merge.final_project.redemption.dto.response.RedemptionResponse;
import com.merge.final_project.redemption.entity.Redemption;
import com.merge.final_project.redemption.repository.RedemptionRepository;
import com.merge.final_project.wallet.entity.Wallet;
import com.merge.final_project.wallet.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.web3j.protocol.core.methods.response.TransactionReceipt;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RedemptionService {

    private final WalletRepository walletRepository;
    private final FoundationRepository foundationRepository;
    private final BeneficiaryRepository beneficiaryRepository;
    private final RedemptionRepository redemptionRepository;
    private final RedemptionCommandService redemptionCommandService;
    private final BlockchainService blockchainService;
    private final TransactionRepository transactionRepository;

    // 현금화된 토큰이 최종적으로 모이는 hot wallet 주소
    @Value("${blockchain.wallet.hot-address}")
    private String hotWalletAddress;

    @Transactional
    public RedemptionResponse requestRedemption(RedemptionRequest request) {
        // 1. 요청 본문의 필수값이 비어 있거나 잘못된 값인지 먼저 확인한다.
        validateRequest(request);

        // 2. 요청자 타입에 따라 재단 또는 수혜자를 조회하고, 현금화에 사용할 지갑을 찾는다.
        Wallet requesterWallet = findRequesterWallet(request.getRequesterType(), request.getRequesterNo());

        // 3. 현금화는 요청자 지갑의 서명이 필요하므로 개인키 존재 여부와 잔액을 먼저 검증한다.
        validateWalletForRedemption(requesterWallet, request.getAmount());
        validateOnChainBalanceForRedemption(requesterWallet, request.getAmount());

        // 4. hot wallet 을 로컬 DB 에서 조회한다.
        // transaction 에 from / to 지갑을 함께 남겨야 이후 관리자 조회에서 흐름을 추적할 수 있다.
        Wallet hotWallet = walletRepository.findByWalletAddress(hotWalletAddress)
                .orElseThrow(() -> new IllegalArgumentException("핫 월렛 정보를 찾을 수 없습니다."));

        // 5. 실제 체인 호출 전에 요청 원본을 먼저 PENDING 상태로 저장한다.
        // 이렇게 해야 체인 호출 자체가 실패해도 어떤 요청이 실패했는지 DB 에 남는다.
        Redemption redemption = redemptionCommandService.createPending(
                request.getRequesterType(),
                request.getRequesterNo(),
                request.getAmount(),
                requesterWallet
        );

        // 6. 체인 호출 직전에 PROCESSING 으로 바꿔두면,
        // 체인 성공 후 로컬 후처리만 실패한 경우를 PENDING 과 구분할 수 있다.
        redemptionCommandService.markProcessing(redemption.getRedemptionNo());

        TransactionReceipt receipt;
        try {
            // 7. 요청자 지갑 개인키로 현금화 컨트랙트를 호출한다.
            // redemptionNo 를 함께 보내서 온체인 처리와 로컬 요청을 연결한다.
            receipt = blockchainService.redeemOnChain(
                    requesterWallet.getKey().getPrivateKey(),
                    BigInteger.valueOf(request.getAmount()),
                    BigInteger.valueOf(redemption.getRedemptionNo())
            );
        } catch (Exception e) {
            // 8. 여기서 실패한 것은 현금화 체인 처리 자체가 실패한 경우이므로 FAILED 로 남긴다.
            redemptionCommandService.markFailed(redemption.getRedemptionNo(), "블록체인 현금화 처리에 실패했습니다.");
            throw new RuntimeException("현금화 온체인 처리에 실패했습니다.", e);
        }

        try {
            // 9. 체인 호출이 성공하면 receipt 값을 기준으로 token_transaction 을 저장한다.
            Transaction transaction = createRedemptionTransaction(
                    requesterWallet,
                    hotWallet,
                    request.getAmount(),
                    receipt
            );
            transactionRepository.save(transaction);

            // 10. 로컬 후처리까지 끝난 경우에만 최종 상태를 COMPLETED 로 확정한다.
            // 이 상태는 관리자 입금 대기 상태로 사용된다.
            redemptionCommandService.markCompleted(
                    redemption.getRedemptionNo(),
                    transaction,
                    transaction.getBlockNum()
            );

            // 11. 로컬에서 계산한 잔액이 아니라 실제 체인 잔액을 다시 읽어와서 지갑 balance 를 맞춘다.
            requesterWallet.updateBalance(
                    BigDecimal.valueOf(blockchainService.getTokenBalance(requesterWallet.getWalletAddress()).longValue())
            );
            requesterWallet.updateLastUsedAt();

            hotWallet.updateBalance(
                    BigDecimal.valueOf(blockchainService.getTokenBalance(hotWallet.getWalletAddress()).longValue())
            );
            hotWallet.updateLastUsedAt();

            return RedemptionResponse.builder()
                    .redemptionNo(redemption.getRedemptionNo())
                    .requesterType(redemption.getRequesterType().name())
                    .amount(redemption.getAmount())
                    .status(RedemptionStatus.COMPLETED)
                    .requestedAt(redemption.getRequestedAt())
                    .processedAt(LocalDateTime.now())
                    .requesterNo(redemption.getRequesterNo())
                    .failureReason(null)
                    .build();
        } catch (Exception e) {
            // 12. 이 구간의 실패는 체인 성공 후 로컬 DB 후처리 실패다.
            // 다시 FAILED 로 바꾸면 재시도 시 중복 현금화가 될 수 있으므로 PROCESSING 에 남겨둔다.
            throw new RuntimeException("현금화 후처리에 실패했습니다.", e);
        }
    }

    // 관리자용 현금화 요청 목록 조회 (최신순)
    @Transactional(readOnly = true)
    public List<RedemptionListResponse> getAdminRedemptions(RedemptionStatus status) {
        RedemptionStatus targetStatus = status != null ? status : RedemptionStatus.COMPLETED;

        return redemptionRepository.findAllByStatusOrderByRequestedAtDesc(targetStatus).stream()
                .map(redemption -> {
                    RequesterSnapshot requesterSnapshot = getRequesterSnapshot(
                            redemption.getRequesterType(),
                            redemption.getRequesterNo()
                    );
                    // 요청자 정보 조회 (이름 등)
                    return RedemptionListResponse.builder()
                            .redemptionNo(redemption.getRedemptionNo())
                            .requesterName(requesterSnapshot.name())
                            .requesterType(redemption.getRequesterType().name())
                            .amount(redemption.getAmount())
                            .status(redemption.getStatus())
                            .requestedAt(redemption.getRequestedAt())
                            // 트랜잭션 없을 수 있으므로 null 처리
                            .txHash(redemption.getTransaction() != null ? redemption.getTransaction().getTxHash() : null)
                            .build();
                })
                .toList();
    }

    // 관리자용 현금화 요청 상세 조회
    @Transactional(readOnly = true)
    public RedemptionDetailResponse getAdminRedemptionDetail(Long redemptionNo) {
        Redemption redemption = redemptionRepository.findById(redemptionNo)
                .orElseThrow(() -> new IllegalArgumentException("현금화 요청 정보를 찾을 수 없습니다."));

        // 요청자 정보 조회
        RequesterSnapshot requesterSnapshot = getRequesterSnapshot(
                redemption.getRequesterType(),
                redemption.getRequesterNo()
        );

        return RedemptionDetailResponse.builder()
                .redemptionNo(redemption.getRedemptionNo())
                .requesterName(requesterSnapshot.name())
                .requesterType(redemption.getRequesterType().name())
                .requesterNo(redemption.getRequesterNo())
                .account(requesterSnapshot.account())
                .walletAddress(redemption.getWallet().getWalletAddress())
                .amount(redemption.getAmount())
                .status(redemption.getStatus())
                .requestedAt(redemption.getRequestedAt())
                .processedAt(redemption.getProcessedAt())
                .cashPaidAt(redemption.getCashPaidAt())
                .blockNumber(redemption.getBlockNumber())
                // 트랜잭션 정보 null 가능
                .transactionNo(redemption.getTransaction() != null ? redemption.getTransaction().getTransactionNo() : null)
                .txHash(redemption.getTransaction() != null ? redemption.getTransaction().getTxHash() : null)
                .failureReason(redemption.getFailureReason())
                .build();
    }

    // 현금 지급 완료 처리 (COMPLETED → PAID)
    @Transactional
    public void markCashPaid(Long redemptionNo) {
        Redemption redemption = redemptionRepository.findById(redemptionNo)
                .orElseThrow(() -> new IllegalArgumentException("현금화 요청 정보를 찾을 수 없습니다."));

        // 이미 지급 완료된 경우 차단
        if (redemption.getStatus() == RedemptionStatus.PAID) {
            throw new IllegalArgumentException("이미 입금 완료 처리된 현금화 요청입니다.");
        }
        // 완료 상태(COMPLETED)가 아닌 경우 차단
        if (redemption.getStatus() != RedemptionStatus.COMPLETED) {
            throw new IllegalArgumentException("입금 완료 처리는 현금화 완료 건에 대해서만 가능합니다.");
        }

        redemptionCommandService.markPaid(redemptionNo);
    }

    // 요청 본문의 필수값이 비어 있는지 먼저 확인한다.
    private void validateRequest(RedemptionRequest request) {
        if (request.getRequesterType() == null) {
            throw new IllegalArgumentException("요청자 타입은 필수입니다.");
        }
        if (request.getRequesterNo() == null) {
            throw new IllegalArgumentException("요청자 번호는 필수입니다.");
        }
        if (request.getAmount() == null || request.getAmount() <= 0) {
            throw new IllegalArgumentException("현금화 금액은 0보다 커야 합니다.");
        }
    }

    // 요청자 타입에 따라 실제 지갑 조회 경로가 다르므로 여기서 분기한다.
    private Wallet findRequesterWallet(RequesterType requesterType, Long requesterNo) {
        if (requesterType == RequesterType.FOUNDATION) {
            Foundation foundation = foundationRepository.findById(requesterNo)
                    .orElseThrow(() -> new IllegalArgumentException("재단 정보를 찾을 수 없습니다."));

            if (foundation.getWallet() == null) {
                throw new IllegalArgumentException("재단 지갑 정보가 없습니다.");
            }
            return foundation.getWallet();
        }

        if (requesterType == RequesterType.BENEFICIARY) {
            Beneficiary beneficiary = beneficiaryRepository.findById(requesterNo)
                    .orElseThrow(() -> new IllegalArgumentException("수혜자 정보를 찾을 수 없습니다."));

            if (beneficiary.getWallet() == null) {
                throw new IllegalArgumentException("수혜자 지갑 정보가 없습니다.");
            }
            return beneficiary.getWallet();
        }

        throw new IllegalArgumentException("지원하지 않는 요청자 타입입니다.");
    }

    // 현금화에 필요한 최소 조건을 사전 검증한다.
    private void validateWalletForRedemption(Wallet wallet, Long amount) {
        if (wallet.getKey() == null || wallet.getKey().getPrivateKey() == null || wallet.getKey().getPrivateKey().isBlank()) {
            throw new IllegalArgumentException("요청자 지갑의 개인키 정보가 없습니다.");
        }
        if (wallet.getBalance() == null) {
            throw new IllegalArgumentException("요청자 지갑 잔액 정보가 없습니다.");
        }
        if (wallet.getBalance().compareTo(BigDecimal.valueOf(amount)) < 0) {
            throw new IllegalArgumentException("현금화 가능한 토큰 잔액이 부족합니다.");
        }
    }

    // 관리자 화면에 보여줄 요청자 이름과 계좌 정보는 requesterType 에 따라 조회한다.
    private RequesterSnapshot getRequesterSnapshot(RequesterType requesterType, Long requesterNo) {
        if (requesterType == RequesterType.FOUNDATION) {
            Foundation foundation = foundationRepository.findById(requesterNo)
                    .orElseThrow(() -> new IllegalArgumentException("재단 정보를 찾을 수 없습니다."));
            return new RequesterSnapshot(foundation.getFoundationName(), foundation.getAccount());
        }

        if (requesterType == RequesterType.BENEFICIARY) {
            Beneficiary beneficiary = beneficiaryRepository.findById(requesterNo)
                    .orElseThrow(() -> new IllegalArgumentException("수혜자 정보를 찾을 수 없습니다."));
            return new RequesterSnapshot(beneficiary.getName(), beneficiary.getAccount());
        }

        throw new IllegalArgumentException("지원하지 않는 요청자 타입입니다.");
    }

    // 체인 영수증을 로컬 token_transaction 엔티티로 변환한다.
    private Transaction createRedemptionTransaction(
            Wallet requesterWallet,
            Wallet hotWallet,
            Long amount,
            TransactionReceipt receipt
    ) {
        String transactionCode = UUID.randomUUID().toString();
        Long blockNum = receipt.getBlockNumber() != null ? receipt.getBlockNumber().longValue() : null;
        BigDecimal gasFee = receipt.getGasUsed() != null
                ? BigDecimal.valueOf(receipt.getGasUsed().longValue())
                : null;

        return Transaction.builder()
                .transactionCode(transactionCode)
                .fromWallet(requesterWallet)
                .toWallet(hotWallet)
                .amount(amount)
                .sentAt(LocalDateTime.now())
                .txHash(receipt.getTransactionHash())
                .blockNum(blockNum)
                .status(TransactionStatus.SUCCESS)
                .gasFee(gasFee)
                .eventType(TransactionEventType.REDEMPTION)
                .createdAt(LocalDateTime.now())
                .build();
    }

    // 온체인 지갑 잔액 검증 (현금화 가능 여부 체크)
    // - 잔액이 0 이하이면 불가
    // - 요청 금액보다 부족하면 불가
    private void validateOnChainBalanceForRedemption(Wallet wallet, Long amount) {
        BigDecimal onChainBalance;
        try {
            // 블록체인에서 실제 토큰 잔액 조회
            onChainBalance = new BigDecimal(blockchainService.getTokenBalance(wallet.getWalletAddress()));
        } catch (Exception e) {
            throw new RuntimeException("요청자 지갑의 온체인 잔액 조회에 실패했습니다.", e);
        }
        // 잔액이 없으면 현금화 불가
        if (onChainBalance.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("현금화 가능한 온체인 토큰 잔액이 없습니다.");
        }
        // 요청 금액보다 잔액이 부족하면 현금화 불가
        if (onChainBalance.compareTo(BigDecimal.valueOf(amount)) < 0) {
            throw new IllegalArgumentException("현금화 가능한 온체인 토큰 잔액이 부족합니다.");
        }
    }

    // 요청자 정보 스냅샷 (이름, 계좌)
    private record RequesterSnapshot(String name, String account) {
    }
}

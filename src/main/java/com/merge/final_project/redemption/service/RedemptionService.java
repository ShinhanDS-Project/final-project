package com.merge.final_project.redemption.service;

import com.merge.final_project.blockchain.entity.Transaction;
import com.merge.final_project.blockchain.entity.TransactionEventType;
import com.merge.final_project.blockchain.entity.TransactionStatus;
import com.merge.final_project.blockchain.repository.TransactionRepository;
import com.merge.final_project.blockchain.security.WalletCryptoService;
import com.merge.final_project.blockchain.service.BlockchainService;
import com.merge.final_project.blockchain.service.TokenAmountConverter;
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
    private final WalletCryptoService walletCryptoService;
    private final TokenAmountConverter tokenAmountConverter;

    @Value("${blockchain.wallet.hot-address}")
    private String hotWalletAddress;

    /**
     * 환급 요청 처리 메인 로직
     *
     * [전체 흐름]
     * 1. 요청값 검증
     * 2. 요청자 지갑 조회 및 잔액 검증
     * 3. hot wallet 조회
     * 4. Redemption 생성 (PENDING -> PROCESSING)
     * 5. 온체인 환급 호출
     * 6. 온체인 성공 직후 ONCHAIN_CONFIRMED 전환
     * 7. 트랜잭션 저장 + 상태 완료 + 지갑 동기화
     */
    @Transactional
    public RedemptionResponse requestRedemption(RedemptionRequest request) {
        // Preserve the direct service entry point used by tests and internal callers.
        if (request.getRequesterType() == null || request.getRequesterNo() == null) {
            throw new IllegalArgumentException("requesterType and requesterNo are required");
        }
        return requestRedemption(request, request.getRequesterType(), request.getRequesterNo());
    }

    @Transactional
    public RedemptionResponse requestRedemption(RedemptionRequest request, RequesterType requesterType, Long requesterNo) {
        validateRequest(request);

        // The authenticated requester decides which wallet is used for the cash-out.
        Wallet requesterWallet = findRequesterWallet(requesterType, requesterNo);
        // 로컬 잔액과 실제 체인 잔액을 둘 다 확인한다.
        validateWalletForRedemption(requesterWallet, request.getAmount());
        validateOnChainBalanceForRedemption(requesterWallet, request.getAmount());

        // 환급 토큰이 모일 서버 hot wallet 조회
        Wallet hotWallet = walletRepository.findByWalletAddress(hotWalletAddress)
                .orElseThrow(() -> new IllegalArgumentException("hot wallet not found"));

        // 환급 요청을 먼저 PENDING 상태로 생성한다.
        Redemption redemption = redemptionCommandService.createPending(
                requesterType,
                requesterNo,
                request.getAmount(),
                requesterWallet
        );

        // 체인 호출 직전에 PROCESSING으로 전환한다.
        redemptionCommandService.markProcessing(redemption.getRedemptionNo());

        TransactionReceipt receipt;
        try {
            // 요청자 지갑 개인키로 환급 컨트랙트를 호출한다.
            receipt = blockchainService.redeemOnChain(
                    resolveWalletPrivateKey(requesterWallet),
                    tokenAmountConverter.toOnChainAmount(request.getAmount()),
                    BigInteger.valueOf(redemption.getRedemptionNo())
            );
        } catch (Exception e) {
            // 온체인 호출 자체가 실패한 경우만 FAILED로 남긴다.
            redemptionCommandService.markFailed(redemption.getRedemptionNo(), "블록체인 현금화 처리에 실패했습니다.");
            throw new RuntimeException("failed to process redemption on chain", e);
        }

        // 체인 환급은 끝났지만 로컬 후처리가 아직 남아 있으므로 중간 상태로 남긴다.
        Long onChainBlockNumber = receipt.getBlockNumber() != null ? receipt.getBlockNumber().longValue() : null;
        redemptionCommandService.markOnChainConfirmed(redemption.getRedemptionNo(), onChainBlockNumber);

        try {
            // 체인 영수증을 기준으로 token_transaction을 남긴다.
            Transaction transaction = createRedemptionTransaction(
                    requesterWallet,
                    hotWallet,
                    request.getAmount(),
                    receipt
            );
            transactionRepository.save(transaction);

            // 환급 상태를 COMPLETED로 변경하고 transaction/blockNumber를 연결한다.
            redemptionCommandService.markCompleted(
                    redemption.getRedemptionNo(),
                    transaction,
                    transaction.getBlockNum()
            );

            // 환급 이후 실제 온체인 잔액을 다시 읽어 로컬 지갑 잔액을 맞춘다.
            requesterWallet.updateBalance(
                    tokenAmountConverter.fromOnChainAmount(
                            blockchainService.getTokenBalance(requesterWallet.getWalletAddress())
                    )
            );
            requesterWallet.updateLastUsedAt();

            hotWallet.updateBalance(
                    tokenAmountConverter.fromOnChainAmount(
                            blockchainService.getTokenBalance(hotWallet.getWalletAddress())
                    )
            );
            hotWallet.updateLastUsedAt();

            // 최종 응답 반환
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
            // 온체인은 성공했지만 로컬 후처리가 실패한 경우 ONCHAIN_CONFIRMED에 남겨 재송금을 막는다.
            throw new RuntimeException("failed to finalize redemption", e);
        }
    }

    // 관리자용 환급 요청 목록 조회
    @Transactional(readOnly = true)
    public List<RedemptionListResponse> getAdminRedemptions(RedemptionStatus status) {
        RedemptionStatus targetStatus = status != null ? status : RedemptionStatus.COMPLETED;

        return redemptionRepository.findAllByStatusOrderByRequestedAtDesc(targetStatus).stream()
                .map(redemption -> {
                    RequesterSnapshot requesterSnapshot = getRequesterSnapshot(
                            redemption.getRequesterType(),
                            redemption.getRequesterNo()
                    );
                    return RedemptionListResponse.builder()
                            .redemptionNo(redemption.getRedemptionNo())
                            .requesterName(requesterSnapshot.name())
                            .requesterType(redemption.getRequesterType().name())
                            .amount(redemption.getAmount())
                            .status(redemption.getStatus())
                            .requestedAt(redemption.getRequestedAt())
                            .txHash(redemption.getTransaction() != null ? redemption.getTransaction().getTxHash() : null)
                            .build();
                })
                .toList();
    }

    // 관리자용 환급 요청 상세 조회
    @Transactional(readOnly = true)
    public RedemptionDetailResponse getAdminRedemptionDetail(Long redemptionNo) {
        Redemption redemption = redemptionRepository.findById(redemptionNo)
                .orElseThrow(() -> new IllegalArgumentException("redemption not found"));

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
                .transactionNo(redemption.getTransaction() != null ? redemption.getTransaction().getTransactionNo() : null)
                .txHash(redemption.getTransaction() != null ? redemption.getTransaction().getTxHash() : null)
                .failureReason(redemption.getFailureReason())
                .build();
    }

    // 입금 완료 처리 (COMPLETED -> PAID)
    @Transactional
    public void markCashPaid(Long redemptionNo) {
        Redemption redemption = redemptionRepository.findById(redemptionNo)
                .orElseThrow(() -> new IllegalArgumentException("redemption not found"));

        if (redemption.getStatus() == RedemptionStatus.PAID) {
            throw new IllegalArgumentException("이미 입금 완료 처리된 현금화 요청입니다.");
        }
        if (redemption.getStatus() != RedemptionStatus.COMPLETED) {
            throw new IllegalArgumentException("입금 완료 처리는 현금화 완료 건에 대해서만 가능합니다.");
        }

        redemptionCommandService.markPaid(redemptionNo);
    }

    // 요청 본문의 필수값 검증
    private void validateRequest(RedemptionRequest request) {
        if (request.getAmount() == null || request.getAmount() <= 0) {
            throw new IllegalArgumentException("amount must be positive");
        }
    }

    // 요청자 타입에 따라 실제 지갑 조회 경로가 다르므로 여기서 분기한다.
    private Wallet findRequesterWallet(RequesterType requesterType, Long requesterNo) {
        if (requesterType == RequesterType.FOUNDATION) {
            Foundation foundation = foundationRepository.findById(requesterNo)
                    .orElseThrow(() -> new IllegalArgumentException("foundation not found"));
            if (foundation.getWallet() == null) {
                throw new IllegalArgumentException("foundation wallet not found");
            }
            return foundation.getWallet();
        }

        if (requesterType == RequesterType.BENEFICIARY) {
            Beneficiary beneficiary = beneficiaryRepository.findById(requesterNo)
                    .orElseThrow(() -> new IllegalArgumentException("beneficiary not found"));
            if (beneficiary.getWallet() == null) {
                throw new IllegalArgumentException("beneficiary wallet not found");
            }
            return beneficiary.getWallet();
        }

        throw new IllegalArgumentException("unsupported requesterType");
    }

    // 환급 요청 전에 최소한의 로컬 지갑 조건을 검증한다.
    private void validateWalletForRedemption(Wallet wallet, Long amount) {
        if (wallet.getKey() == null || wallet.getKey().getPrivateKey() == null || wallet.getKey().getPrivateKey().isBlank()) {
            throw new IllegalArgumentException("requester private key not found");
        }
        if (wallet.getBalance() == null) {
            throw new IllegalArgumentException("requester wallet balance not found");
        }
        if (wallet.getBalance().compareTo(BigDecimal.valueOf(amount)) < 0) {
            throw new IllegalArgumentException("insufficient token balance");
        }
    }

    // 관리자 화면에 보여줄 요청자 이름/계좌를 requesterType 기준으로 조회한다.
    private RequesterSnapshot getRequesterSnapshot(RequesterType requesterType, Long requesterNo) {
        if (requesterType == RequesterType.FOUNDATION) {
            Foundation foundation = foundationRepository.findById(requesterNo)
                    .orElseThrow(() -> new IllegalArgumentException("foundation not found"));
            return new RequesterSnapshot(foundation.getFoundationName(), foundation.getAccount());
        }

        if (requesterType == RequesterType.BENEFICIARY) {
            Beneficiary beneficiary = beneficiaryRepository.findById(requesterNo)
                    .orElseThrow(() -> new IllegalArgumentException("beneficiary not found"));
            return new RequesterSnapshot(beneficiary.getName(), beneficiary.getAccount());
        }

        throw new IllegalArgumentException("unsupported requesterType");
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

    // 실제 온체인 잔액을 기준으로 환급 가능 여부를 다시 검증한다.
    private void validateOnChainBalanceForRedemption(Wallet wallet, Long amount) {
        BigDecimal onChainBalance;
        try {
            onChainBalance = tokenAmountConverter.fromOnChainAmount(
                    blockchainService.getTokenBalance(wallet.getWalletAddress())
            );
        } catch (Exception e) {
            throw new RuntimeException("failed to read requester on-chain balance", e);
        }
        if (onChainBalance.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("on-chain token balance is empty");
        }
        if (onChainBalance.compareTo(BigDecimal.valueOf(amount)) < 0) {
            throw new IllegalArgumentException("insufficient on-chain token balance");
        }
    }

    /**
     * 요청자 지갑의 private key를 복호화한다.
     * 과거 평문 데이터와의 호환을 위해 복호화 실패 시 raw 문자열을 그대로 사용한다.
     */
    private String resolveWalletPrivateKey(Wallet wallet) {
        String storedPrivateKey = wallet.getKey().getPrivateKey();
        if (storedPrivateKey == null || storedPrivateKey.isBlank()) {
            throw new IllegalArgumentException("requester private key not found");
        }

        try {
            return walletCryptoService.decryptPrivateKey(storedPrivateKey);
        } catch (RuntimeException e) {
            return storedPrivateKey;
        }
    }

    private record RequesterSnapshot(String name, String account) {
    }
}

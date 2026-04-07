package com.merge.final_project.redemption.service;

import com.merge.final_project.blockchain.entity.Transaction;
import com.merge.final_project.blockchain.entity.TransactionEventType;
import com.merge.final_project.blockchain.entity.TransactionStatus;
import com.merge.final_project.blockchain.repository.TransactionRepository;
import com.merge.final_project.blockchain.service.BlockchainService;
import com.merge.final_project.org.foundation.Foundation;
import com.merge.final_project.org.foundation.FoundationRepository;
import com.merge.final_project.recipient.beneficiary.Beneficiary;
import com.merge.final_project.recipient.beneficiary.BeneficiaryRepository;
import com.merge.final_project.redemption.RedemptionStatus;
import com.merge.final_project.redemption.RequesterType;
import com.merge.final_project.redemption.dto.request.RedemptionRequest;
import com.merge.final_project.redemption.dto.response.RedemptionResponse;
import com.merge.final_project.redemption.entity.Redemption;
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
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RedemptionService {

    private final WalletRepository walletRepository;
    private final FoundationRepository foundationRepository;
    private final BeneficiaryRepository beneficiaryRepository;
    private final RedemptionCommandService redemptionCommandService;
    private final BlockchainService blockchainService;
    private final TransactionRepository transactionRepository;

    // 현금화 시 토큰이 최종적으로 모이는 hot wallet 주소다.
    @Value("${blockchain.wallet.hot-address}")
    private String hotWalletAddress;

    @Transactional
    public RedemptionResponse requestRedemption(RedemptionRequest request) {
        // 1. 요청 본문에 필수값이 빠졌는지 가장 먼저 확인한다.
        validateRequest(request);

        // 2. 요청자 타입에 따라 재단 또는 수혜자를 조회하고, 현금화에 사용할 요청자 지갑을 찾는다
        Wallet requesterWallet = findRequesterWallet(request.getRequesterType(), request.getRequesterNo());

        // 3. 현금화는 요청자 지갑이 서명하므로 개인키 존재 여부와 잔액을 먼저 확인한다.
        validateWalletForRedemption(requesterWallet, request.getAmount());

        // 4. hot wallet 을 로컬 DB 에서 조회한다.
        // 그래야 token_transaction 에서 from/to 지갑 관계를 함께 남길 수 있다.
        Wallet hotWallet = walletRepository.findByWalletAddress(hotWalletAddress)
                .orElseThrow(() -> new IllegalArgumentException("핫 월렛 정보를 찾을 수 없습니다."));

        // 5. 온체인 호출 전, 현금화 요청 원본을 먼저 PENDING 으로 저장한다.
        // 이렇게 해야 블록체인 호출이 실패해도 어떤 요청이 실패했는지 DB 에 남는다.
        Redemption redemption = redemptionCommandService.createPending(
                request.getRequesterType(),
                request.getRequesterNo(),
                request.getAmount(),
                requesterWallet
        );

        // 6. 실제 체인 호출 직전에 PROCESSING 으로 바꿔둔다.
        // 체인 성공 후 로컬 후처리만 실패한 건을 PENDING 과 구분하기 위한 상태다.
        redemptionCommandService.markProcessing(redemption.getRedemptionNo());

        TransactionReceipt receipt;
        try {
            // 7. 요청자 지갑의 개인키로 현금화 컨트랙트 함수를 호출한다.
            // redemptionNo 도 함께 넘겨서 온체인 처리와 로컬 요청을 연결한다.
            receipt = blockchainService.redeemOnChain(
                    requesterWallet.getKey().getPrivateKey(),
                    BigInteger.valueOf(request.getAmount()),
                    BigInteger.valueOf(redemption.getRedemptionNo())
            );
        } catch (Exception e) {
            // 8. 이 구간에서 실패했다는 것은 온체인 현금화 자체가 실패한 경우이므로
            // 요청 상태를 FAILED 로 남겨 이후 원인 추적과 재처리 판단이 가능하게 한다.
            redemptionCommandService.markFailed(redemption.getRedemptionNo(), "블록체인 현금화 처리에 실패했습니다.");
            throw new RuntimeException("현금화 온체인 처리에 실패했습니다.", e);
        }

        try {
            // 9. 온체인 호출이 성공하면 영수증 값을 기준으로 token_transaction 을 저장한다.
            Transaction transaction = createRedemptionTransaction(
                    requesterWallet,
                    hotWallet,
                    request.getAmount(),
                    receipt
            );
            transactionRepository.save(transaction);

            // 10. 로컬 후처리까지 정상 반영된 경우에만 최종 상태를 COMPLETED 로 바꾼다.
            redemptionCommandService.markCompleted(
                    redemption.getRedemptionNo(),
                    transaction,
                    transaction.getBlockNum()
            );

            // 11. 로컬 지갑 잔액도 실제 현금화 결과에 맞춰 차감하고 마지막 사용 시각을 갱신한다.
            //요청자의 지갑 토큰 체인에서 읽어와 반영
            requesterWallet.updateBalance(
                    BigDecimal.valueOf(blockchainService.getTokenBalance(requesterWallet.getWalletAddress()).longValue())
            );
            //지갑 사용시간 갱신
            requesterWallet.updateLastUsedAt();

            //핫월렛도 토큰을 체인에서 읽어와서 반영
            hotWallet.updateBalance(
                    BigDecimal.valueOf(blockchainService.getTokenBalance(hotWallet.getWalletAddress()).longValue())
            );
            //사용시간 갱신
            hotWallet.updateLastUsedAt();

            // 12. 호출자는 요청 결과를 바로 확인할 수 있도록
            // 주요 상태값을 응답으로 받는다.
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
            // 13. 여기서 실패했다는 것은 체인 호출은 이미 성공했고
            // 로컬 DB 반영만 실패했다는 뜻이다.
            // 이 상태를 FAILED 로 내려버리면 재시도 시 중복 현금화가 날 수 있으므로
            // 상태는 PROCESSING 으로 남겨두고 예외만 올린다.
            throw new RuntimeException("현금화 후처리에 실패했습니다.", e);
        }
    }

    // 요청 본문에 필수값이 빠졌는지 가장 먼저 확인한다.
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
        // 기부단체 요청이면 재단 엔티티에서 연결 지갑을 가져온다.
        if (requesterType == RequesterType.FOUNDATION) {
            Foundation foundation = foundationRepository.findById(requesterNo)
                    .orElseThrow(() -> new IllegalArgumentException("기부단체 정보를 찾을 수 없습니다."));

            if (foundation.getWallet() == null) {
                throw new IllegalArgumentException("기부단체 지갑 정보가 없습니다.");
            }
            return foundation.getWallet();
        }

        // 수혜자 요청이면 수혜자 엔티티에서 연결 지갑을 가져온다.
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

    // 현금화 가능한 지갑인지 사전 검증한다.
    private void validateWalletForRedemption(Wallet wallet, Long amount) {
        // 개인키가 없으면 요청자 지갑으로 직접 서명할 수 없다.
        if (wallet.getKey() == null || wallet.getKey().getPrivateKey() == null || wallet.getKey().getPrivateKey().isBlank()) {
            throw new IllegalArgumentException("요청자 지갑의 개인키 정보가 없습니다.");
        }
        // 잔액이 없거나 부족하면 온체인 호출 전에 차단한다.
        if (wallet.getBalance() == null) {
            throw new IllegalArgumentException("요청자 지갑 잔액 정보가 없습니다.");
        }
        if (wallet.getBalance().compareTo(BigDecimal.valueOf(amount)) < 0) {
            throw new IllegalArgumentException("현금화 가능한 토큰 잔액이 부족합니다.");
        }
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
                .amount(amount.intValue())
                .sentAt(LocalDateTime.now())
                .txHash(receipt.getTransactionHash())
                .blockNum(blockNum)
                .status(TransactionStatus.SUCCESS)
                .gasFee(gasFee)
                .eventType(TransactionEventType.REDEMPTION)
                .createdAt(LocalDateTime.now())
                .build();
    }
}

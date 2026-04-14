package com.merge.final_project.campaign.settlement.service;

import com.merge.final_project.blockchain.entity.Transaction;
import com.merge.final_project.blockchain.entity.TransactionEventType;
import com.merge.final_project.blockchain.entity.TransactionStatus;
import com.merge.final_project.blockchain.repository.TransactionRepository;
import com.merge.final_project.blockchain.service.BlockchainService;
import com.merge.final_project.blockchain.service.TokenAmountConverter;
import com.merge.final_project.campaign.campaigns.CampaignStatus;
import com.merge.final_project.campaign.campaigns.entity.Campaign;
import com.merge.final_project.campaign.settlement.Repository.SettlementRepository;
import com.merge.final_project.campaign.settlement.Settlement;
import com.merge.final_project.campaign.settlement.SettlementStatus;
import com.merge.final_project.wallet.entity.Wallet;
import com.merge.final_project.wallet.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class SettlementRecoveryService {

    private final SettlementRepository settlementRepository;
    private final SettlementCommandService settlementCommandService;
    private final TransactionRepository transactionRepository;
    private final WalletRepository walletRepository;
    private final BlockchainService blockchainService;
    private final TokenAmountConverter tokenAmountConverter;

    // 온체인 완료된 정산 복구 실행
    public void recoverOnChainConfirmedSettlements() {
        // ONCHAIN_CONFIRMED 상태 조회
        List<Settlement> settlements = settlementRepository.findAllByStatusOrderBySettlementNoAsc(
                SettlementStatus.ONCHAIN_CONFIRMED
        );
        log.info("settlement recovery scan started. pendingCount={}", settlements.size());
        // 하나씩 복구 처리
        for (Settlement settlement : settlements) {
            try {
                finalizeConfirmedSettlement(settlement.getSettlementNo());
                log.info("settlement recovery completed. settlementNo={}", settlement.getSettlementNo());
            } catch (Exception e) {
                log.error("settlement recovery failed. settlementNo={}", settlement.getSettlementNo(), e);
            }
        }
    }

    // 온체인 완료된 정산을 DB에 최종 반영 (체인 호출 없음)
    @Transactional
    public void finalizeConfirmedSettlement(Long settlementNo) {
        // 정산 조회
        Settlement settlement = settlementRepository.findById(settlementNo)
                .orElseThrow(() -> new IllegalArgumentException("settlement not found"));
        // 상태가 ONCHAIN_CONFIRMED 아니면 스킵
        if (settlement.getStatus() != SettlementStatus.ONCHAIN_CONFIRMED) {
            return;
        }

        // 캠페인 및 관련 지갑 조회
        Campaign campaign = settlement.getCampaign();
        Wallet campaignWallet = walletRepository.findById(campaign.getWalletNo())
                .orElseThrow(() -> new IllegalArgumentException("campaign wallet not found"));
        Wallet foundationWallet = settlement.getFoundation().getWallet();
        Wallet beneficiaryWallet = settlement.getBeneficiary().getWallet();

        // 필수 지갑 체크
        if (foundationWallet == null) {
            throw new IllegalArgumentException("foundation wallet not found");
        }
        if (beneficiaryWallet == null) {
            throw new IllegalArgumentException("beneficiary wallet not found");
        }

        // 기존 트랜잭션 조회 (transactionCode 기준)
        List<Transaction> existingTransactions = transactionRepository.findByTransactionCode(settlement.getTransactionCode());
        // 이벤트 타입별로 중복 방지용 Map 생성
        Map<TransactionEventType, Transaction> existingByType = new EnumMap<>(TransactionEventType.class);
        for (Transaction transaction : existingTransactions) {
            existingByType.putIfAbsent(transaction.getEventType(), transaction);
        }
        // 기존 트랜잭션 중 하나를 메타데이터 기준으로 사용
        Transaction metadataSource = existingTransactions.isEmpty() ? null : existingTransactions.get(0);

        // 현재 시간
        LocalDateTime now = LocalDateTime.now();

        // 수수료 트랜잭션 없으면 생성 (캠페인 → 기부단체)
        if (!existingByType.containsKey(TransactionEventType.SETTLEMENT_FEE)) {
            transactionRepository.save(
                    buildSettlementTransaction(
                            settlement.getTransactionCode(),
                            campaignWallet,
                            foundationWallet,
                            settlement.getFoundationAmount(),
                            TransactionEventType.SETTLEMENT_FEE,
                            metadataSource,
                            now
                    )
            );
        }
        // 수혜자 트랜잭션 없으면 생성 (캠페인 → 수혜자)
        if (!existingByType.containsKey(TransactionEventType.SETTLEMENT_BENEFICIARY)) {
            transactionRepository.save(
                    buildSettlementTransaction(
                            settlement.getTransactionCode(),
                            campaignWallet,
                            beneficiaryWallet,
                            settlement.getBeneficiaryAmount(),
                            TransactionEventType.SETTLEMENT_BENEFICIARY,
                            metadataSource,
                            now
                    )
            );
        }

        // 캠페인 상태를 SETTLED로 변경
        campaign.setCampaignStatus(CampaignStatus.SETTLED);

        // 모든 관련 지갑 잔액 동기화
        syncWalletBalance(campaignWallet);
        syncWalletBalance(foundationWallet);
        syncWalletBalance(beneficiaryWallet);
        // 정산 상태 COMPLETED 처리
        settlementCommandService.markCompleted(settlement.getSettlementNo());
    }

    // 정산 트랜잭션 생성 (공통 빌더)
    private Transaction buildSettlementTransaction(
            String transactionCode,
            Wallet fromWallet,
            Wallet toWallet,
            Long amount,
            TransactionEventType eventType,
            Transaction metadataSource,
            LocalDateTime now
    ) {
        return Transaction.builder()
                .transactionCode(transactionCode)
                .fromWallet(fromWallet)
                .toWallet(toWallet)
                .amount(amount)
                .sentAt(resolveSentAt(metadataSource, now))
                .txHash(resolveTxHash(metadataSource))
                .blockNum(resolveBlockNum(metadataSource))
                .status(TransactionStatus.SUCCESS)
                .gasFee(resolveGasFee(metadataSource))
                .eventType(eventType)
                .createdAt(now)
                .build();
    }

    // 지갑 잔액을 블록체인 기준으로 동기화
    private void syncWalletBalance(Wallet wallet) {
        try {
            wallet.updateBalance(
                    tokenAmountConverter.fromOnChainAmount(
                            blockchainService.getTokenBalance(wallet.getWalletAddress())
                    )
            );
            wallet.updateLastUsedAt();  // 마지막 사용 시간 갱신
        } catch (Exception e) {
            throw new RuntimeException("failed to sync wallet balance", e);
        }
    }

    // sentAt 값 복구 (없으면 현재 시간 사용)
    private LocalDateTime resolveSentAt(Transaction transaction, LocalDateTime fallback) {
        return transaction != null && transaction.getSentAt() != null ? transaction.getSentAt() : fallback;
    }

    // txHash 복구
    private String resolveTxHash(Transaction transaction) {
        return transaction != null ? transaction.getTxHash() : null;
    }

    // blockNum 복구
    private Long resolveBlockNum(Transaction transaction) {
        return transaction != null ? transaction.getBlockNum() : null;
    }

    // gasFee 복구
    private BigDecimal resolveGasFee(Transaction transaction) {
        return transaction != null ? transaction.getGasFee() : null;
    }
}

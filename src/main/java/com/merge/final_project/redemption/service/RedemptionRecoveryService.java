package com.merge.final_project.redemption.service;

import com.merge.final_project.blockchain.entity.Transaction;
import com.merge.final_project.blockchain.entity.TransactionEventType;
import com.merge.final_project.blockchain.entity.TransactionStatus;
import com.merge.final_project.blockchain.repository.TransactionRepository;
import com.merge.final_project.blockchain.service.BlockchainService;
import com.merge.final_project.blockchain.service.TokenAmountConverter;
import com.merge.final_project.blockchain.wallet.HotWalletResolver;
import com.merge.final_project.redemption.RedemptionStatus;
import com.merge.final_project.redemption.entity.Redemption;
import com.merge.final_project.redemption.repository.RedemptionRepository;
import com.merge.final_project.wallet.entity.Wallet;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class RedemptionRecoveryService {

    private final RedemptionRepository redemptionRepository;
    private final RedemptionCommandService redemptionCommandService;
    private final HotWalletResolver hotWalletResolver;
    private final TransactionRepository transactionRepository;
    private final BlockchainService blockchainService;
    private final TokenAmountConverter tokenAmountConverter;

    @Value("${blockchain.wallet.hot-address}")
    private String hotWalletAddress;

    // 온체인 완료 상태지만 로컬 반영 안된 환전 데이터 복구 실행
    public void recoverOnChainConfirmedRedemptions() {
        // ONCHAIN_CONFIRMED 상태 조회
        List<Redemption> redemptions = redemptionRepository.findAllByStatusOrderByRedemptionNoAsc(
                RedemptionStatus.ONCHAIN_CONFIRMED
        );
        log.info("redemption recovery scan started. pendingCount={}", redemptions.size());
        // 하나씩 복구 처리
        for (Redemption redemption : redemptions) {
            try {
                finalizeConfirmedRedemption(redemption.getRedemptionNo());
                log.info("redemption recovery completed. redemptionNo={}", redemption.getRedemptionNo());
            } catch (Exception e) {
                log.error("redemption recovery failed. redemptionNo={}", redemption.getRedemptionNo(), e);
            }
        }
    }

    // 온체인 완료된 환전을 로컬 DB에 최종 반영 (체인 호출 없음)
    @Transactional
    public void finalizeConfirmedRedemption(Long redemptionNo) {
        // 환전 조회
        Redemption redemption = redemptionRepository.findById(redemptionNo)
                .orElseThrow(() -> new IllegalArgumentException("redemption not found"));
        // 상태가 ONCHAIN_CONFIRMED 아니면 스킵
        if (redemption.getStatus() != RedemptionStatus.ONCHAIN_CONFIRMED) {
            return;
        }
        // 요청자 지갑
        Wallet requesterWallet = redemption.getWallet();
        // HOT 지갑 조회
        Wallet hotWallet = resolveHotWallet();

        // 기존 트랜잭션 확인
        Transaction transaction = redemption.getTransaction();
        // 트랜잭션 없으면 새로 생성 (복구 목적)
        if (transaction == null) {
            transaction = transactionRepository.save(
                    Transaction.builder()
                            .transactionCode(UUID.randomUUID().toString())
                            .fromWallet(requesterWallet)
                            .toWallet(hotWallet)
                            .amount(redemption.getAmount())
                            .sentAt(LocalDateTime.now())
                            .txHash(null)
                            .blockNum(redemption.getBlockNumber())
                            .status(TransactionStatus.SUCCESS)
                            .gasFee(null)
                            .eventType(TransactionEventType.REDEMPTION)
                            .createdAt(LocalDateTime.now())
                            .build()
            );
        }
        // 요청자 지갑 잔액 동기화
        syncWalletBalance(requesterWallet);
        // HOT 지갑 잔액 동기화
        syncWalletBalance(hotWallet);
        // 환전 상태 COMPLETED로 변경
        redemptionCommandService.markCompleted(redemption.getRedemptionNo(), transaction, redemption.getBlockNumber());
    }

    // 블록체인 기준으로 지갑 잔액 동기화
    private void syncWalletBalance(Wallet wallet) {
        try {
            wallet.updateBalance(
                    tokenAmountConverter.fromOnChainAmount(
                            blockchainService.getTokenBalance(wallet.getWalletAddress())    // 온체인 잔액 조회
                    )
            );
            wallet.updateLastUsedAt();  // 마지막 사용 시간 갱신
        } catch (Exception e) {
            throw new RuntimeException("failed to sync wallet balance", e);
        }
    }

    private Wallet resolveHotWallet() {
        return hotWalletResolver.resolve(hotWalletAddress);
    }
}

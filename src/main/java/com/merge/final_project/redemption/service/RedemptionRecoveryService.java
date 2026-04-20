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
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

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

    public void recoverOnChainConfirmedRedemptions() {
        List<Redemption> redemptions = redemptionRepository.findAllByStatusOrderByRedemptionNoAsc(
                RedemptionStatus.ONCHAIN_CONFIRMED
        );
        log.info("redemption recovery scan started. pendingCount={}", redemptions.size());
        for (Redemption redemption : redemptions) {
            try {
                finalizeConfirmedRedemption(redemption.getRedemptionNo());
                log.info("redemption recovery completed. redemptionNo={}", redemption.getRedemptionNo());
            } catch (Exception e) {
                log.error("redemption recovery failed. redemptionNo={}", redemption.getRedemptionNo(), e);
            }
        }
    }

    @Transactional
    public void finalizeConfirmedRedemption(Long redemptionNo) {
        // 동시 크론 실행 시 동일 redemption을 중복 처리하지 않도록 행 잠금으로 조회
        Redemption redemption = redemptionRepository.findByIdForUpdate(redemptionNo)
                .orElseThrow(() -> new IllegalArgumentException("redemption not found"));

        // 이미 완료된 건은 멱등하게 즉시 종료
        if (redemption.getStatus() == RedemptionStatus.COMPLETED) {
            return;
        }
        // 복구 대상은 ONCHAIN_CONFIRMED 상태만 허용
        if (redemption.getStatus() != RedemptionStatus.ONCHAIN_CONFIRMED) {
            return;
        }

        Wallet requesterWallet = redemption.getWallet();
        Wallet hotWallet = resolveHotWallet();

        Transaction transaction = redemption.getTransaction();
        if (transaction == null) {
            // 기존 성공 tx가 있으면 재사용(재전송/재생성 방지)
            transaction = findExistingRecoveryTransaction(redemption, requesterWallet, hotWallet);
        }
        if (transaction == null) {
            // 정말 없는 경우에만 신규 생성
            transaction = createRecoveryTransaction(redemption, requesterWallet, hotWallet);
        }

        // 최종 완료 전 양쪽 지갑 잔액을 체인 기준으로 동기화
        syncWalletBalance(requesterWallet);
        syncWalletBalance(hotWallet);
        redemptionCommandService.markCompleted(redemption.getRedemptionNo(), transaction, redemption.getBlockNumber());
    }

    private Transaction createRecoveryTransaction(Redemption redemption, Wallet requesterWallet, Wallet hotWallet) {
        try {
            return transactionRepository.save(
                    Transaction.builder()
                            .transactionCode("REDEMPTION-RECOVERY-" + redemption.getRedemptionNo())
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
        } catch (DataIntegrityViolationException ex) {
            // 동시성으로 유니크 충돌이 나면 실패시키지 않고 기존 tx 재조회 후 재연결
            Transaction existing = findExistingRecoveryTransaction(redemption, requesterWallet, hotWallet);
            if (existing != null) {
                log.info("redemption recovery reused existing transaction after conflict. redemptionNo={}, transactionNo={}",
                        redemption.getRedemptionNo(), existing.getTransactionNo());
                return existing;
            }
            throw ex;
        }
    }

    private Transaction findExistingRecoveryTransaction(Redemption redemption, Wallet requesterWallet, Wallet hotWallet) {
        return transactionRepository
                .findTopByEventTypeAndStatusAndBlockNumAndAmountAndFromWallet_WalletNoAndToWallet_WalletNoOrderByTransactionNoDesc(
                        TransactionEventType.REDEMPTION,
                        TransactionStatus.SUCCESS,
                        redemption.getBlockNumber(),
                        redemption.getAmount(),
                        requesterWallet.getWalletNo(),
                        hotWallet.getWalletNo()
                )
                .orElse(null);
    }

    private void syncWalletBalance(Wallet wallet) {
        try {
            wallet.updateBalance(
                    tokenAmountConverter.fromOnChainAmount(
                            blockchainService.getTokenBalance(wallet.getWalletAddress())
                    )
            );
            wallet.updateLastUsedAt();
        } catch (Exception e) {
            throw new RuntimeException("failed to sync wallet balance", e);
        }
    }

    private Wallet resolveHotWallet() {
        return hotWalletResolver.resolve(hotWalletAddress);
    }
}

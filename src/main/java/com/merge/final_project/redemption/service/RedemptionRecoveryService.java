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
        Redemption redemption = redemptionRepository.findByIdForUpdate(redemptionNo)
                .orElseThrow(() -> new IllegalArgumentException("redemption not found"));

        if (redemption.getStatus() == RedemptionStatus.COMPLETED) {
            return;
        }
        if (redemption.getStatus() != RedemptionStatus.ONCHAIN_CONFIRMED) {
            return;
        }

        Wallet requesterWallet = redemption.getWallet();
        Wallet hotWallet = resolveHotWallet();

        Transaction transaction = redemption.getTransaction();
        if (transaction == null) {
            transaction = findExistingRecoveryTransaction(redemption, requesterWallet, hotWallet);
        }
        if (transaction == null) {
            transaction = createRecoveryTransaction(redemption, requesterWallet, hotWallet);
        }

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

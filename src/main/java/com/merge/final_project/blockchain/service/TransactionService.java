package com.merge.final_project.blockchain.service;

import com.merge.final_project.db.entity.TokenTransaction;
import com.merge.final_project.db.entity.Wallet;
import com.merge.final_project.db.repository.TokenTransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TokenTransactionRepository tokenTransactionRepository;

    /**
     * 이벤트 파싱 전 공통 기본값으로 token_transaction row를 생성한다.
     */
    public TokenTransaction saveTransfer(Wallet fromWallet,
                                         Wallet toWallet,
                                         Long amount,
                                         String txHash,
                                         Long blockNumber,
                                         String status,
                                         String eventType) {
        TokenTransaction transaction = new TokenTransaction();
        transaction.setFromWalletNo(fromWallet == null ? null : toWalletNo(fromWallet));
        transaction.setToWalletNo(toWallet == null ? null : toWalletNo(toWallet));
        transaction.setAmount(safeLongToInt(amount));
        transaction.setSentAt(LocalDateTime.now());
        transaction.setTxHash(txHash);
        transaction.setBlockNum(blockNumber == null ? null : safeLongToInt(blockNumber));
        transaction.setStatus(status);
        transaction.setGasFee(0);
        transaction.setEventType(eventType);
        transaction.setCreatedAt(LocalDateTime.now());
        transaction.setTransactionCode(UUID.randomUUID().toString());
        return tokenTransactionRepository.save(transaction);
    }

    /**
     * 운영 스키마 int 컬럼 범위를 넘는 값은 경계값으로 보정한다.
     */
    private Integer safeLongToInt(Long value) {
        if (value == null) {
            return null;
        }
        if (value > Integer.MAX_VALUE) {
            return Integer.MAX_VALUE;
        }
        if (value < Integer.MIN_VALUE) {
            return Integer.MIN_VALUE;
        }
        return value.intValue();
    }

    private Integer toWalletNo(Wallet wallet) {
        if (wallet.getId() == null) {
            return null;
        }
        return safeLongToInt(wallet.getId().getWalletNo());
    }
}

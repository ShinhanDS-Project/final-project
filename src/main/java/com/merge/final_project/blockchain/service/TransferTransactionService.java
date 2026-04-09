package com.merge.final_project.blockchain.service;

import com.merge.final_project.blockchain.entity.Transaction;
import com.merge.final_project.blockchain.entity.TransactionEventType;
import com.merge.final_project.blockchain.entity.TransactionStatus;
import com.merge.final_project.blockchain.repository.TransactionRepository;
import com.merge.final_project.wallet.entity.Wallet;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TransferTransactionService {

    private final TransactionRepository transactionRepository;

    public Transaction saveTransfer(
            Wallet fromWallet,
            Wallet toWallet,
            Long amount,
            String txHash,
            Long blockNumber,
            String status,
            String eventType
    ) {
        Transaction transaction = Transaction.builder()
                .transactionCode(UUID.randomUUID().toString())
                .fromWallet(fromWallet)
                .toWallet(toWallet)
                .amount(amount)
                .sentAt(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .txHash(txHash)
                .blockNum(blockNumber)
                .status(toStatus(status))
                .gasFee(BigDecimal.ZERO)
                .eventType(toEventType(eventType))
                .build();
        return transactionRepository.save(transaction);
    }

    private TransactionStatus toStatus(String status) {
        if (status == null) {
            return TransactionStatus.FAILED;
        }
        try {
            return TransactionStatus.valueOf(status.toUpperCase(Locale.ROOT));
        } catch (Exception ignored) {
            return TransactionStatus.FAILED;
        }
    }

    private TransactionEventType toEventType(String eventType) {
        if (eventType == null || eventType.isBlank()) {
            return TransactionEventType.DONATION;
        }
        try {
            return TransactionEventType.valueOf(eventType.toUpperCase(Locale.ROOT));
        } catch (Exception ignored) {
            return TransactionEventType.DONATION;
        }
    }
}

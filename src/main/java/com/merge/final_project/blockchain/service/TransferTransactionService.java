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

    /**
     * 블록체인 전송 결과 1건을 Transaction 테이블에 저장한다.
     * 전송 진입점에서 공통으로 사용해 저장 포맷을 일관되게 유지한다.
     */
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

    /**
     * 전송 클라이언트의 문자열 status를 내부 TransactionStatus enum으로 매핑한다.
     * 알 수 없는 값은 안전하게 FAILED로 처리한다.
     */
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

    /**
     * 문자열 eventType을 내부 TransactionEventType enum으로 매핑한다.
     * 알 수 없는 값은 null 방지를 위해 DONATION으로 대체한다.
     */
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

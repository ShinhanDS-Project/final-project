package com.merge.final_project.admin.dto;

import com.merge.final_project.blockchain.entity.Transaction;
import com.merge.final_project.blockchain.entity.TransactionEventType;
import com.merge.final_project.blockchain.entity.TransactionStatus;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
public class AdminTransactionDTO {

    private Long transactionNo;
    private String fromWalletAddress;
    private String toWalletAddress;
    private Long amount;
    private TransactionEventType eventType;
    private TransactionStatus status;
    private String txHash;
    private Long blockNum;
    private BigDecimal gasFee;
    private LocalDateTime sentAt;
    private LocalDateTime createdAt;

    public static AdminTransactionDTO from(Transaction transaction) {
        return AdminTransactionDTO.builder()
                .transactionNo(transaction.getTransactionNo())
                .fromWalletAddress(transaction.getFromWallet().getWalletAddress())
                .toWalletAddress(transaction.getToWallet().getWalletAddress())
                .amount(transaction.getAmount())
                .eventType(transaction.getEventType())
                .status(transaction.getStatus())
                .txHash(transaction.getTxHash())
                .blockNum(transaction.getBlockNum())
                .gasFee(transaction.getGasFee())
                .sentAt(transaction.getSentAt())
                .createdAt(transaction.getCreatedAt())
                .build();
    }
}

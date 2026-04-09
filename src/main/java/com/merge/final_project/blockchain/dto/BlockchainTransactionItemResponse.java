package com.merge.final_project.blockchain.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record BlockchainTransactionItemResponse(
        String transactionCode,
        String txHash,
        Long blockNum,
        String status,
        String eventType,
        String eventTypeLabel,
        Long amount,
        BigDecimal gasFee,
        LocalDateTime sentAt,
        String fromWalletAddress,
        String toWalletAddress,
        String foundationName,
        String campaignName,
        String memo,
        String fromOwnerTypeLabel,
        String toOwnerTypeLabel
) {
}

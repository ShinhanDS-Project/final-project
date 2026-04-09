package com.merge.final_project.blockchain.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record BlockchainTransactionDetailResponse(
        String transactionCode,
        String txHash,
        Long blockNum,
        String status,
        String eventType,
        String eventTypeLabel,
        Long amount,
        LocalDateTime sentAt,
        BigDecimal gasFee,
        String foundationName,
        String campaignName,
        String memo,
        String fromOwnerTypeLabel,
        String toOwnerTypeLabel,
        BlockchainWalletSummaryResponse fromWallet,
        BlockchainWalletSummaryResponse toWallet
) {
}

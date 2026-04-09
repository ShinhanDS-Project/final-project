package com.merge.final_project.blockchain.dto;

import java.math.BigDecimal;

public record BlockchainWalletSummaryResponse(
        String walletAddress,
        String foundationName,
        String campaignName,
        BigDecimal balance,
        String ownerType,
        String ownerTypeLabel
) {
}

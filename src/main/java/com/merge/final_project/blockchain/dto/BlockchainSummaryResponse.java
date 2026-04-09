package com.merge.final_project.blockchain.dto;

import java.math.BigDecimal;

public record BlockchainSummaryResponse(
        Long latestBlock,
        BigDecimal avgBlockTimeSec,
        long totalTx,
        Long tokenAmount
) {
}

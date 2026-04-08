package com.merge.final_project.blockchain.tx;

import java.math.BigInteger;

public record TransferResult(
        String txHash,
        Long blockNumber,
        String status,
        String message,
        String eventType,
        String fromAddress,
        String toAddress,
        BigInteger donationId,
        BigInteger campaignId,
        BigInteger onChainAmount
) {
}

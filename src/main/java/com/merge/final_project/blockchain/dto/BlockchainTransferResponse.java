package com.merge.final_project.blockchain.dto;

public record BlockchainTransferResponse(
        String transactionNo,
        String txHash,
        String status,
        String eventType
) {
}

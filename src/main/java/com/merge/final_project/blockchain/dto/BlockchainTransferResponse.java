package com.merge.final_project.blockchain.dto;

/**
 * 블록체인 전송 실행 + DB 저장 이후 반환하는 API 응답.
 */
public record BlockchainTransferResponse(
        String transactionNo,
        String txHash,
        String status,
        String eventType
) {
}

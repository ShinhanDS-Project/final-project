package com.merge.final_project.blockchain.dto;

/**
 * 전송 실행 API의 최소 응답 DTO.
 * 성공/실패 여부와 추적에 필요한 핵심 식별자만 반환한다.
 */
public record BlockchainTransferResponse(
        // DB transaction_no
        String transactionNo,
        // 체인 tx hash
        String txHash,
        // SUCCESS / FAILED
        String status,
        // 저장된 이벤트 타입
        String eventType
) {
}

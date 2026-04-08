package com.merge.final_project.blockchain.tx;

import java.math.BigInteger;

/**
 * 서비스 계층 공통 전송 결과 모델.
 * 트랜잭션 메타데이터 + 파싱된 이벤트 메타데이터를 함께 담는다.
 */
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

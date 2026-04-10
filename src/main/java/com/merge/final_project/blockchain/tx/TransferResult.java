package com.merge.final_project.blockchain.tx;

import java.math.BigInteger;

/**
 * 블록체인 전송 클라이언트의 표준 결과 DTO.
 * 성공/실패 여부와 체인 메타데이터를 서비스 계층으로 전달한다.
 */
public record TransferResult(
        // 체인에 전송된 트랜잭션 해시
        String txHash,
        // 포함된 블록 번호(미확정/실패 시 null 가능)
        Long blockNumber,
        // SUCCESS / FAIL
        String status,
        // 실패 사유 또는 참고 메시지
        String message,
        // 도메인 이벤트 타입(ALLOCATION, DONATION 등)
        String eventType,
        // 송신자 주소(필요 시)
        String fromAddress,
        // 수신자 주소
        String toAddress,
        // 도메인 기부 식별자
        BigInteger donationId,
        // 도메인 캠페인 식별자
        BigInteger campaignId,
        // 체인에 전달된 최소 단위 토큰/코인 수량
        BigInteger onChainAmount
) {
}

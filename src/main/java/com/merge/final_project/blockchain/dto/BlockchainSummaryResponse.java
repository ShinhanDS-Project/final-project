package com.merge.final_project.blockchain.dto;

import java.math.BigDecimal;

/**
 * 블록체인 대시보드 요약 응답 DTO.
 */
public record BlockchainSummaryResponse(
        // 상태 필터 기준 최신 블록 번호
        Long latestBlock,
        // 평균 블록 간격(초 단위, 소수점 2자리)
        BigDecimal avgBlockTimeSec,
        // 상태 필터 기준 총 거래 수
        long totalTx,
        // 토큰 관련 이벤트 금액 합계
        Long tokenAmount
) {
}

package com.merge.final_project.blockchain.dto;

import java.math.BigDecimal;

/**
 * 지갑 요약 정보 DTO.
 */
public record BlockchainWalletSummaryResponse(
        // 지갑 주소
        String walletAddress,
        // 연관 단체명
        String foundationName,
        // 연관 캠페인명
        String campaignName,
        // 잔액
        BigDecimal balance,
        // API 표준 owner 타입 코드
        String ownerType,
        // 화면 표시용 owner 타입 라벨
        String ownerTypeLabel
) {
}

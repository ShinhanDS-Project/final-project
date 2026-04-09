package com.merge.final_project.blockchain.dto;

import java.util.List;

/**
 * 지갑 상세 API 응답 DTO.
 * 지갑 기본 정보와 연관 거래 목록을 함께 제공한다.
 */
public record BlockchainWalletDetailResponse(
        // 지갑 요약 정보
        BlockchainWalletSummaryResponse wallet,
        // 지갑 연관 거래 목록
        List<BlockchainTransactionItemResponse> items,
        // 페이징 메타정보
        BlockchainPageInfoResponse pageInfo
) {
}

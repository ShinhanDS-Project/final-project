package com.merge.final_project.blockchain.dto;

import java.util.List;

/**
 * 거래 목록 API 응답 DTO.
 */
public record BlockchainTransactionsResponse(
        // 현재 페이지의 거래 아이템 목록
        List<BlockchainTransactionItemResponse> items,
        // 페이징 메타정보
        BlockchainPageInfoResponse pageInfo
) {
}

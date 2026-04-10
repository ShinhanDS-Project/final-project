package com.merge.final_project.blockchain.dto;

/**
 * 대시보드 리스트 응답의 공통 페이징 메타데이터.
 */
public record BlockchainPageInfoResponse(
        // 현재 페이지(1-base)
        int page,
        // 페이지 크기
        int pageSize,
        // 전체 아이템 수
        long totalItems,
        // 전체 페이지 수
        int totalPages
) {
}

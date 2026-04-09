package com.merge.final_project.blockchain.dto;

public record BlockchainPageInfoResponse(
        int page,
        int pageSize,
        long totalItems,
        int totalPages
) {
}

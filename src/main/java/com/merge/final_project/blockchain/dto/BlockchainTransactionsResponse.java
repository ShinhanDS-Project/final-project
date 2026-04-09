package com.merge.final_project.blockchain.dto;

import java.util.List;

public record BlockchainTransactionsResponse(
        List<BlockchainTransactionItemResponse> items,
        BlockchainPageInfoResponse pageInfo
) {
}

package com.merge.final_project.blockchain.dto;

import java.util.List;

public record BlockchainWalletDetailResponse(
        BlockchainWalletSummaryResponse wallet,
        List<BlockchainTransactionItemResponse> items,
        BlockchainPageInfoResponse pageInfo
) {
}

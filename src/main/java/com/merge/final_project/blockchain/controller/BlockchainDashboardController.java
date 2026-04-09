package com.merge.final_project.blockchain.controller;

import com.merge.final_project.blockchain.dto.BlockchainSearchResolveResponse;
import com.merge.final_project.blockchain.dto.BlockchainSummaryResponse;
import com.merge.final_project.blockchain.dto.BlockchainTransactionDetailResponse;
import com.merge.final_project.blockchain.dto.BlockchainTransactionsResponse;
import com.merge.final_project.blockchain.dto.BlockchainWalletDetailResponse;
import com.merge.final_project.blockchain.service.BlockchainDashboardQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/blockchain")
@RequiredArgsConstructor
public class BlockchainDashboardController {

    private final BlockchainDashboardQueryService blockchainDashboardQueryService;

    @GetMapping("/transactions")
    public ResponseEntity<BlockchainTransactionsResponse> getTransactions(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "SUCCESS") String status
    ) {
        return ResponseEntity.ok(
                blockchainDashboardQueryService.getTransactions(page, keyword, status)
        );
    }

    @GetMapping("/summary")
    public ResponseEntity<BlockchainSummaryResponse> getSummary(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "SUCCESS") String status
    ) {
        return ResponseEntity.ok(
                blockchainDashboardQueryService.getSummary(page, status)
        );
    }

    @GetMapping("/transactions/{txHash}")
    public ResponseEntity<BlockchainTransactionDetailResponse> getTransactionDetail(
            @PathVariable String txHash,
            @RequestParam(defaultValue = "SUCCESS") String status
    ) {
        return ResponseEntity.ok(
                blockchainDashboardQueryService.getTransactionDetail(txHash, status)
        );
    }

    @GetMapping("/wallets/{walletAddress}")
    public ResponseEntity<BlockchainWalletDetailResponse> getWalletDetail(
            @PathVariable String walletAddress,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "SUCCESS") String status
    ) {
        return ResponseEntity.ok(
                blockchainDashboardQueryService.getWalletDetail(walletAddress, page, status)
        );
    }

    @GetMapping("/search/resolve")
    public ResponseEntity<BlockchainSearchResolveResponse> resolveSearchTarget(
            @RequestParam String keyword
    ) {
        return ResponseEntity.ok(
                blockchainDashboardQueryService.resolveSearchTarget(keyword)
        );
    }
}

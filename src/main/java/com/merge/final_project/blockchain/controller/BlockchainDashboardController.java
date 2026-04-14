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

    /**
     * 블록체인 거래 목록 조회 엔드포인트.
     * page: 1부터 시작하는 페이지 번호
     * keyword: txHash/거래코드/지갑주소/이름 검색어
     * status: SUCCESS/FAILED 등 상태 필터
     */
    @GetMapping("/transactions")
    public ResponseEntity<BlockchainTransactionsResponse> getTransactions(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "SUCCESS") String status
    ) {
        return ResponseEntity.ok(
                blockchainDashboardQueryService.getTransactions(page, size, keyword, status)
        );
    }

    /**
     * 대시보드 상단 요약 정보 조회.
     * 최신 블록 번호, 평균 블록 간격, 총 트랜잭션 수, 토큰 집계값을 반환한다.
     */
    @GetMapping("/summary")
    public ResponseEntity<BlockchainSummaryResponse> getSummary(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "SUCCESS") String status
    ) {
        return ResponseEntity.ok(
                blockchainDashboardQueryService.getSummary(page, status)
        );
    }

    /**
     * txHash 기준 거래 상세 조회.
     * 요청한 해시가 없으면 서비스 계층에서 404(ResponseStatusException)로 처리한다.
     */
    @GetMapping("/transactions/{txHash}")
    public ResponseEntity<BlockchainTransactionDetailResponse> getTransactionDetail(
            @PathVariable String txHash,
            @RequestParam(defaultValue = "SUCCESS") String status
    ) {
        return ResponseEntity.ok(
                blockchainDashboardQueryService.getTransactionDetail(txHash, status)
        );
    }

    /**
     * 지갑 주소 기준 상세 조회.
     * 지갑 기본 정보 + 해당 지갑이 참여한 거래 목록(페이징)을 함께 반환한다.
     */
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

    /**
     * 검색어를 실제 이동 대상 타입으로 해석한다.
     * 반환 type: transaction / wallet / not_found
     */
    @GetMapping("/search/resolve")
    public ResponseEntity<BlockchainSearchResolveResponse> resolveSearchTarget(
            @RequestParam String keyword
    ) {
        return ResponseEntity.ok(
                blockchainDashboardQueryService.resolveSearchTarget(keyword)
        );
    }
}

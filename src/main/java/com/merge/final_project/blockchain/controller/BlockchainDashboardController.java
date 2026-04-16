package com.merge.final_project.blockchain.controller;

import com.merge.final_project.blockchain.dto.BlockchainSearchResolveResponse;
import com.merge.final_project.blockchain.dto.BlockchainSummaryResponse;
import com.merge.final_project.blockchain.dto.BlockchainTransactionDetailResponse;
import com.merge.final_project.blockchain.dto.BlockchainTransactionsResponse;
import com.merge.final_project.blockchain.dto.BlockchainWalletDetailResponse;
import com.merge.final_project.blockchain.service.BlockchainDashboardQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@io.swagger.v3.oas.annotations.tags.Tag(name = "블록체인 대시보드", description = "블록체인 거래 내역·지갑·요약 조회 API (공개)")
@RestController
@RequestMapping("/api/blockchain")
@RequiredArgsConstructor
public class BlockchainDashboardController {

    private final BlockchainDashboardQueryService blockchainDashboardQueryService;

    @Operation(summary = "블록체인 거래 목록 조회", description = "txHash·거래코드·지갑주소·이름으로 검색하고 상태로 필터링한 블록체인 거래 목록을 조회합니다. 인증 불필요.")
    @ApiResponses({ @ApiResponse(responseCode = "200", description = "조회 성공") })
    @GetMapping("/transactions")
    public ResponseEntity<BlockchainTransactionsResponse> getTransactions(
            @Parameter(description = "페이지 번호 (1부터 시작)", example = "1") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "페이지 크기", example = "10") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "검색어 (txHash·거래코드·지갑주소·이름)", example = "0xabc") @RequestParam(defaultValue = "") String keyword,
            @Parameter(description = "거래 상태 필터 (SUCCESS, FAILED)", example = "SUCCESS") @RequestParam(defaultValue = "SUCCESS") String status
    ) {
        return ResponseEntity.ok(
                blockchainDashboardQueryService.getTransactions(page, size, keyword, status)
        );
    }

    @Operation(summary = "블록체인 대시보드 요약 조회", description = "최신 블록 번호·평균 블록 간격·총 트랜잭션 수·토큰 집계값을 반환합니다. 인증 불필요.")
    @ApiResponses({ @ApiResponse(responseCode = "200", description = "조회 성공") })
    @GetMapping("/summary")
    public ResponseEntity<BlockchainSummaryResponse> getSummary(
            @Parameter(description = "페이지 번호", example = "1") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "상태 필터", example = "SUCCESS") @RequestParam(defaultValue = "SUCCESS") String status
    ) {
        return ResponseEntity.ok(
                blockchainDashboardQueryService.getSummary(page, status)
        );
    }

    @Operation(summary = "블록체인 거래 상세 조회", description = "txHash로 특정 거래의 상세 정보를 조회합니다. 인증 불필요.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "거래를 찾을 수 없음")
    })
    @GetMapping("/transactions/{txHash}")
    public ResponseEntity<BlockchainTransactionDetailResponse> getTransactionDetail(
            @Parameter(description = "트랜잭션 해시", example = "0xabc123...") @PathVariable String txHash,
            @Parameter(description = "상태 필터", example = "SUCCESS") @RequestParam(defaultValue = "SUCCESS") String status
    ) {
        return ResponseEntity.ok(
                blockchainDashboardQueryService.getTransactionDetail(txHash, status)
        );
    }

    @Operation(summary = "지갑 상세 조회", description = "지갑 주소로 기본 정보와 해당 지갑이 참여한 거래 목록을 페이징 조회합니다. 인증 불필요.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "지갑을 찾을 수 없음")
    })
    @GetMapping("/wallets/{walletAddress}")
    public ResponseEntity<BlockchainWalletDetailResponse> getWalletDetail(
            @Parameter(description = "지갑 주소", example = "0x1234...") @PathVariable String walletAddress,
            @Parameter(description = "페이지 번호", example = "1") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "상태 필터", example = "SUCCESS") @RequestParam(defaultValue = "SUCCESS") String status
    ) {
        return ResponseEntity.ok(
                blockchainDashboardQueryService.getWalletDetail(walletAddress, page, status)
        );
    }

    @Operation(summary = "검색어 타입 판별", description = "검색어가 트랜잭션 해시인지 지갑 주소인지 판별합니다. 반환 type: transaction / wallet / not_found. 인증 불필요.")
    @ApiResponses({ @ApiResponse(responseCode = "200", description = "판별 성공") })
    @GetMapping("/search/resolve")
    public ResponseEntity<BlockchainSearchResolveResponse> resolveSearchTarget(
            @Parameter(description = "검색어 (txHash 또는 지갑 주소)", example = "0xabc123...") @RequestParam String keyword
    ) {
        return ResponseEntity.ok(
                blockchainDashboardQueryService.resolveSearchTarget(keyword)
        );
    }
}

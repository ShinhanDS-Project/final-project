package com.merge.final_project.redemption.controller;

import com.merge.final_project.redemption.RedemptionStatus;
import com.merge.final_project.redemption.dto.response.RedemptionDetailResponse;
import com.merge.final_project.redemption.dto.response.RedemptionListResponse;
import com.merge.final_project.redemption.service.RedemptionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@io.swagger.v3.oas.annotations.tags.Tag(name = "관리자 환금 관리", description = "관리자 토큰 환금(현금화) 처리 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/redemptions")
public class AdminRedemptionController {

    private final RedemptionService redemptionService;

    @Operation(summary = "환금 요청 목록 조회", description = "토큰 환금(현금화) 요청 목록을 상태 필터로 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 요청"),
            @ApiResponse(responseCode = "403", description = "관리자 권한 없음")
    })
    @GetMapping
    public ResponseEntity<List<RedemptionListResponse>> getRedemptions(
            @Parameter(description = "환금 상태 필터 (PENDING, COMPLETED, PAID 등)", example = "PENDING")
            @RequestParam(required = false) RedemptionStatus status
    ) {
        return ResponseEntity.ok(redemptionService.getAdminRedemptions(status));
    }

    @Operation(summary = "환금 요청 상세 조회", description = "특정 환금 요청의 상세 정보를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "환금 요청을 찾을 수 없음"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 요청"),
            @ApiResponse(responseCode = "403", description = "관리자 권한 없음")
    })
    @GetMapping("/{redemptionNo}")
    public ResponseEntity<RedemptionDetailResponse> getRedemptionDetail(
            @Parameter(description = "환금 요청 번호", example = "1") @PathVariable Long redemptionNo) {
        return ResponseEntity.ok(redemptionService.getAdminRedemptionDetail(redemptionNo));
    }

    @Operation(summary = "현금 지급 완료 처리", description = "환금 요청에 대해 현금 지급이 완료되었음을 처리합니다. COMPLETED → PAID 상태 전이.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "처리 성공"),
            @ApiResponse(responseCode = "404", description = "환금 요청을 찾을 수 없음"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 요청"),
            @ApiResponse(responseCode = "403", description = "관리자 권한 없음")
    })
    @PatchMapping("/{redemptionNo}/pay")
    public ResponseEntity<Void> markCashPaid(
            @Parameter(description = "환금 요청 번호", example = "1") @PathVariable Long redemptionNo) {
        redemptionService.markCashPaid(redemptionNo);
        return ResponseEntity.ok().build();
    }
}

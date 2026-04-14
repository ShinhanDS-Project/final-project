package com.merge.final_project.admin.controller;

import com.merge.final_project.admin.service.AdminFinalReportService;
import com.merge.final_project.report.finalreport.dto.FinalReportResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "관리자 활동 보고서 관리", description = "관리자 활동 보고서 조회·승인·반려 API")
@RestController
@RequestMapping("/admin/reports")
@RequiredArgsConstructor
public class AdminFinalReportController {

    private final AdminFinalReportService adminFinalReportService;

    @Operation(summary = "승인 대기 활동 보고서 목록 조회", description = "승인 대기 상태의 활동 보고서 목록을 최신순으로 페이징 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 요청"),
            @ApiResponse(responseCode = "403", description = "관리자 권한 없음")
    })
    @GetMapping("/pending")
    public ResponseEntity<Page<FinalReportResponseDTO>> getPendingReports(
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(adminFinalReportService.getPendingReports(pageable));
    }

    @Operation(summary = "활동 보고서 승인", description = "활동 보고서를 승인합니다. 승인 시 캠페인 상태가 COMPLETED로 전환되고 수혜자에게 인앱 알림이 발송됩니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "승인 성공"),
            @ApiResponse(responseCode = "404", description = "보고서를 찾을 수 없음"),
            @ApiResponse(responseCode = "409", description = "이미 처리된 보고서"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 요청"),
            @ApiResponse(responseCode = "403", description = "관리자 권한 없음")
    })
    @PatchMapping("/{reportNo}/approve")
    public ResponseEntity<Void> approve(
            @Parameter(description = "활동 보고서 번호", example = "1") @PathVariable Long reportNo) {
        adminFinalReportService.approveReport(reportNo);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "활동 보고서 반려", description = "활동 보고서를 반려합니다. 반려 사유가 저장되고 수혜자에게 인앱 알림이 발송됩니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "반려 성공"),
            @ApiResponse(responseCode = "404", description = "보고서를 찾을 수 없음"),
            @ApiResponse(responseCode = "409", description = "이미 처리된 보고서"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 요청"),
            @ApiResponse(responseCode = "403", description = "관리자 권한 없음")
    })
    @PatchMapping("/{reportNo}/reject")
    public ResponseEntity<Void> reject(
            @Parameter(description = "활동 보고서 번호", example = "1") @PathVariable Long reportNo,
            @Parameter(description = "반려 사유", required = true, example = "보고 내용이 부정확합니다.") @RequestParam String reason) {
        adminFinalReportService.rejectReport(reportNo, reason);
        return ResponseEntity.ok().build();
    }
}

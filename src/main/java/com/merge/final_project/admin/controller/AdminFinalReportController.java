package com.merge.final_project.admin.controller;

import com.merge.final_project.admin.service.AdminFinalReportService;
import com.merge.final_project.report.finalreport.ReportApprovalStatus;
import com.merge.final_project.report.finalreport.dto.FinalReportResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/reports")
@RequiredArgsConstructor
public class AdminFinalReportController {

    private final AdminFinalReportService adminFinalReportService;

    // 활동 보고서 단건 상세 조회
    @GetMapping("/{reportNo}")
    public ResponseEntity<FinalReportResponseDTO> getReport(@PathVariable Long reportNo) {
        return ResponseEntity.ok(adminFinalReportService.getReport(reportNo));
    }

    // 승인 대기 활동 보고서 목록 (기본: 최신순)
    @GetMapping("/pending")
    public ResponseEntity<Page<FinalReportResponseDTO>> getPendingReports(
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(adminFinalReportService.getPendingReports(pageable));
    }

    // 보고서 조회 목록(기본: 승인/반려 포함, 최신순) - 필요 시 상태 필터 가능
    @GetMapping
    public ResponseEntity<Page<FinalReportResponseDTO>> getReports(
            @RequestParam(required = false) ReportApprovalStatus approvalStatus,
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(adminFinalReportService.getReports(approvalStatus, pageable));
    }

    // 활동 보고서 승인
    @PatchMapping("/{reportNo}/approve")
    public ResponseEntity<Void> approve(@PathVariable Long reportNo) {
        adminFinalReportService.approveReport(reportNo);
        return ResponseEntity.ok().build();
    }

    // 활동 보고서 반려
    @PatchMapping("/{reportNo}/reject")
    public ResponseEntity<Void> reject(@PathVariable Long reportNo,
                                       @RequestParam String reason) {
        adminFinalReportService.rejectReport(reportNo, reason);
        return ResponseEntity.ok().build();
    }
}

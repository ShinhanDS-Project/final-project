package com.merge.final_project.admin.controller;

import com.merge.final_project.admin.adminlog.AdminLogResponseDTO;
import com.merge.final_project.admin.dashboard.AdminDashboardService;
import com.merge.final_project.admin.dashboard.dto.CategoryRatioDTO;
import com.merge.final_project.admin.dashboard.dto.DashboardSummaryDTO;
import com.merge.final_project.admin.dashboard.dto.DonationTrendDTO;
import com.merge.final_project.admin.dashboard.dto.UserRegistrationTrendDTO;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "관리자 대시보드", description = "관리자 대시보드 요약·차트·로그 조회 API")
@RestController
@RequestMapping("/admin/dashboard")
@RequiredArgsConstructor
public class AdminDashboardController {

    private final AdminDashboardService adminDashboardService;

    @Operation(summary = "대시보드 요약 조회", description = "오늘 기부액, 진행 중 캠페인 수, 신규 단체 신청 수, 목표달성 비율 카드 4개와 전체 사용자·누적 기부금 배너를 반환합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 요청"),
            @ApiResponse(responseCode = "403", description = "관리자 권한 없음")
    })
    @GetMapping("/summary")
    public ResponseEntity<DashboardSummaryDTO> getSummary() {
        return ResponseEntity.ok(adminDashboardService.getSummary());
    }

    @Operation(summary = "기부금 추이 차트 조회", description = "최근 N일간 일별 기부금 합계를 반환합니다. days=7, 14, 30 중 선택 가능합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 요청"),
            @ApiResponse(responseCode = "403", description = "관리자 권한 없음")
    })
    @GetMapping("/donation-trend")
    public ResponseEntity<List<DonationTrendDTO>> getDonationTrend(
            @Parameter(description = "조회 기간 (일수, 예: 7, 14, 30)", example = "14")
            @RequestParam(defaultValue = "14") int days) {
        return ResponseEntity.ok(adminDashboardService.getDonationTrend(days));
    }

    @Operation(summary = "카테고리별 비중 차트 조회", description = "카테고리별 캠페인 수와 기부금 합계를 반환합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 요청"),
            @ApiResponse(responseCode = "403", description = "관리자 권한 없음")
    })
    @GetMapping("/category-ratio")
    public ResponseEntity<List<CategoryRatioDTO>> getCategoryRatio() {
        return ResponseEntity.ok(adminDashboardService.getCategoryRatio());
    }

    @Operation(summary = "사용자 가입 추이 차트 조회", description = "최근 N일간 일별 신규 사용자 가입 수를 반환합니다. days=7, 14, 30 중 선택 가능합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 요청"),
            @ApiResponse(responseCode = "403", description = "관리자 권한 없음")
    })
    @GetMapping("/user-registration-trend")
    public ResponseEntity<List<UserRegistrationTrendDTO>> getUserRegistrationTrend(
            @Parameter(description = "조회 기간 (일수, 예: 7, 14, 30)", example = "14")
            @RequestParam(defaultValue = "14") int days) {
        return ResponseEntity.ok(adminDashboardService.getUserRegistrationTrend(days));
    }

    @Operation(summary = "최근 활동 로그 조회", description = "대시보드 초기 로드 시 사용하는 최근 관리자 활동 로그를 최신순 페이징으로 반환합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 요청"),
            @ApiResponse(responseCode = "403", description = "관리자 권한 없음")
    })
    @GetMapping("/recent-logs")
    public ResponseEntity<Page<AdminLogResponseDTO>> getRecentLogs(
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(adminDashboardService.getRecentLogs(pageable));
    }
}

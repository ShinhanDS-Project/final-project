package com.merge.final_project.admin.controller;

import com.merge.final_project.admin.adminlog.AdminLogResponseDTO;
import com.merge.final_project.admin.dashboard.AdminDashboardService;
import com.merge.final_project.admin.dashboard.dto.CategoryRatioDTO;
import com.merge.final_project.admin.dashboard.dto.DashboardSummaryDTO;
import com.merge.final_project.admin.dashboard.dto.DonationTrendDTO;
import com.merge.final_project.admin.dashboard.dto.UserRegistrationTrendDTO;
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

@RestController
@RequestMapping("/admin/dashboard")
@RequiredArgsConstructor
public class AdminDashboardController {

    private final AdminDashboardService adminDashboardService;

    // 요약 카드 4개 + 배너
    @GetMapping("/summary")
    public ResponseEntity<DashboardSummaryDTO> getSummary() {
        return ResponseEntity.ok(adminDashboardService.getSummary());
    }

    // 기부금 추이 차트 (days=7|14|30)
    @GetMapping("/donation-trend")
    public ResponseEntity<List<DonationTrendDTO>> getDonationTrend(
            @RequestParam(defaultValue = "14") int days) {
        return ResponseEntity.ok(adminDashboardService.getDonationTrend(days));
    }

    // 카테고리별 비중 차트 (캠페인 수 + 기부금)
    @GetMapping("/category-ratio")
    public ResponseEntity<List<CategoryRatioDTO>> getCategoryRatio() {
        return ResponseEntity.ok(adminDashboardService.getCategoryRatio());
    }

    // 일별 사용자 가입 추이 차트 (days=7|14|30)
    @GetMapping("/user-registration-trend")
    public ResponseEntity<List<UserRegistrationTrendDTO>> getUserRegistrationTrend(
            @RequestParam(defaultValue = "14") int days) {
        return ResponseEntity.ok(adminDashboardService.getUserRegistrationTrend(days));
    }

    // 최근 활동 로그 (새로운 요청 소식 — REST initial load)
    @GetMapping("/recent-logs")
    public ResponseEntity<Page<AdminLogResponseDTO>> getRecentLogs(
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(adminDashboardService.getRecentLogs(pageable));
    }
}

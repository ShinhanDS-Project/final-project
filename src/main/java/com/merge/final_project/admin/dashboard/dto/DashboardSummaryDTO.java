package com.merge.final_project.admin.dashboard.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Schema(description = "관리자 대시보드 요약 DTO")
@Getter
@Builder
public class DashboardSummaryDTO {

    @Schema(description = "오늘 기부 총액 (원)", example = "1500000")
    private BigDecimal todayDonationAmount;

    @Schema(description = "현재 진행 중인 캠페인 수 (ACTIVE 상태)", example = "23")
    private long activeCampaignCount;

    @Schema(description = "신규 기부단체 가입 신청 대기 수 (PENDING 상태)", example = "5")
    private long pendingFoundationCount;

    @Schema(description = "캠페인 목표 달성 비율 (0~100 %)", example = "72.5")
    private double achievedCampaignRatio;

    @Schema(description = "전체 사용자 수 (배너용)", example = "10240")
    private long totalUserCount;

    @Schema(description = "플랫폼 누적 기부 총액 (배너용, 원)", example = "987654321")
    private BigDecimal totalDonationAmount;
}

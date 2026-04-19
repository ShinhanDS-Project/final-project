package com.merge.final_project.admin.dashboard.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class DashboardSummaryDTO {

    // 요약 카드 4개
    private BigDecimal todayDonationAmount;
    private long activeCampaignCount;
    private long pendingFoundationCount;
    private double achievedCampaignRatio;   // 0~100 (%)

    // 배너
    private long totalUserCount;
    private BigDecimal totalDonationAmount;

    // [가빈] 캠페인 상태별 카운트
    private long endedCampaignCount;
    private long settledCampaignCount;
}

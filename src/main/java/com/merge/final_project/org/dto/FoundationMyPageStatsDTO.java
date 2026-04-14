package com.merge.final_project.org.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Schema(description = "기부단체 마이페이지 통계 DTO")
@Getter
@Builder
public class FoundationMyPageStatsDTO {

    @Schema(description = "현재 진행 중인 캠페인 수 (ACTIVE 상태)", example = "3")
    private long activeCampaignCount;

    @Schema(description = "이번 달 총 모금액 (원)", example = "1500000")
    private BigDecimal thisMonthDonationAmount;
}

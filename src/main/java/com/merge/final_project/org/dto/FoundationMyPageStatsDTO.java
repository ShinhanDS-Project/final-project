package com.merge.final_project.org.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class FoundationMyPageStatsDTO {

    private long activeCampaignCount;       // 진행 중인 캠페인 수
    private BigDecimal thisMonthDonationAmount; // 이번달 모금액
}

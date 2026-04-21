package com.merge.final_project.ai.dto;

import com.merge.final_project.campaign.campaigns.CampaignCategory;
import lombok.*;

import java.math.BigDecimal;

/**
 * [AI 추천] 추천된 캠페인 정보를 담는 응답 DTO
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecommendedCampaignResponse {
    private Long campaignNo;
    private String title;
    private String imagePath;
    private CampaignCategory category;
    private Long targetAmount;
    private BigDecimal currentAmount;
    private double achievementRate; // 달성률
    private String recommendationReason; // 추천 이유 (예: "소액 기부가 절실한 캠페인입니다!")
}

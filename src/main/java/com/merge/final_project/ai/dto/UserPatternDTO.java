package com.merge.final_project.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * [AI 추천] 사용자의 기부 패턴 분석 결과 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserPatternDTO {
    private Long userNo;
    private BigDecimal averageDonationAmount; // 평균 기부 금액
    private long totalDonationCount;          // 총 기부 횟수
    private com.merge.final_project.campaign.campaigns.CampaignCategory favoriteCategory; // 가장 선호하는 카테고리
    private DonorType donorType;              // 분석된 기부자 유형

    public enum DonorType {
        SMALL_DONOR,   // 소액 기부자 (평균 1만원 미만)
        LARGE_DONOR,   // 고액 기부자 (평균 10만원 이상)
        FREQUENT_DONOR, // 열혈 기부자 (금액 상관없이 횟수 위주)
        NEWBIE         // 기부 이력이 아직 적음
    }
}

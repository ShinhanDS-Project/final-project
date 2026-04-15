package com.merge.final_project.donation.donations.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HomeCampaignItemDTO {
    private Long campaignNo;
    private String imagePath;
    private String title;
    private String foundationName;
    private Long targetAmount;
    private Long currentAmount;
    private String category;
    private Integer progressPercent;
    private Long daysLeft;
    private String endAt; // ISO 문자열
}

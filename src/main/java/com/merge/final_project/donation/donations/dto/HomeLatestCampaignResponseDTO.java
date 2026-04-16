package com.merge.final_project.donation.donations.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HomeLatestCampaignResponseDTO {

    private Long campaignNo;
    private String title;
    private String imagePath;
    private String foundationName;
    private String category;
    private Long currentAmount;
    private Long targetAmount;
    private Integer progressPercent;
    private String endAt;
}
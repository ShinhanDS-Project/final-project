package com.merge.final_project.campaign.campaigns.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CampaignListResponseDTO {
    private Long campaignNo;
    private String imagePath;
    private String title;
    private String foundationName;
    private Long targetAmount;
    private Long currentAmount;
    private String category;
    private LocalDateTime endAt;
}

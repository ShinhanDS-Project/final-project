package com.merge.final_project.campaign.campaigns.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CampaignRegisterResponseDTO {
    private Long campaignNo;
    private Long foundationNo;
    private String approvalStatus;
    private String campaignStatus;
    private String message;
}

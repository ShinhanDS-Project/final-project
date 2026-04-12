package com.merge.final_project.campaign.campaigns.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CampaignBeneficiaryCheckResponseDTO {
    private boolean valid;
    private Long beneficiaryNo;
    private String entryCode;
    private String name;
    private String beneficiaryType;
    private String message;
}

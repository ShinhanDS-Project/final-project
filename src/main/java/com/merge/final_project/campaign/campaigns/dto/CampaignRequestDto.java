package com.merge.final_project.campaign.campaigns.dto;

import com.merge.final_project.campaign.campaigns.entity.Campaign;
import com.merge.final_project.campaign.useplan.dto.UsePlanRequestDto;
import lombok.Data;

import java.util.List;

@Data
public class CampaignRequestDto {
    private String title;
    private String description;
    private String category;
    private Long targetAmount;
    private Long beneficiaryNo;
    private List<UsePlanRequestDto> usePlans;

    // DTO -> Entity
    public Campaign toEntity() {
        return Campaign.builder()
                .title(this.title)
                .description(this.description)
                .category(this.category)
                .targetAmount(this.targetAmount.intValue())
                .beneficiaryNo(this.beneficiaryNo.intValue())
                .build();
    }
}

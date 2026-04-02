package com.merge.final_project.campaign.campaigns.dto;

import com.merge.final_project.campaign.campaigns.entity.Campaign;
import lombok.Data;

@Data
public class CampaignRequestDto {
    private String title;
    private String description;
    private String category;
    private Long targetAmount;
    private Long beneficiaryNo;
    private List<UsePlanRequestDto> usePlans; // 지출 계획 리스트 포함

    // DTO -> Entity
    public Campaign toEntity() {
        return Campaign.builder()
                .title(this.title)
                .description(this.description)
                .category(this.category)
                .targetAmount(this.targetAmount)
                .beneficiaryNo(this.beneficiaryNo)
                .build();
    }
}

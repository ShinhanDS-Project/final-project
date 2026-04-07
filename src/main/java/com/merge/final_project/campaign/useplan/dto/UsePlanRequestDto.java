package com.merge.final_project.campaign.useplan.dto;

import com.merge.final_project.campaign.useplan.entity.UsePlan;
import lombok.Data;

@Data
public class UsePlanRequestDto {
    private String planContent;
    private Long planAmount;

    public UsePlan toEntity(Long campaignNo) {
        return UsePlan.builder()
                .planContent(this.planContent)
                .planAmount(this.planAmount)
                .campaignNo(campaignNo)
                .build();
    }
}

package com.merge.final_project.campaign.useplan.dto;

import com.merge.final_project.campaign.useplan.entity.UsePlan;
import lombok.Data;

@Data
public class UsePlanRequestDTO {
    private String planContent;
    private Long planAmount;

    // DTO -> 엔티티 변환
    public UsePlan toEntity(Long campaignNo) {
        return UsePlan.builder()
                .planContent(this.planContent)
                .planAmount(this.planAmount)
                .campaignNo(campaignNo)
                .build();
    }
}

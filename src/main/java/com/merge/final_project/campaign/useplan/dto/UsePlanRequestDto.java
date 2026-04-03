package com.merge.final_project.campaign.useplan.dto;

import com.merge.final_project.campaign.useplan.entity.UsePlan;
import lombok.Data;

@Data
public class UsePlanRequestDto {
    private String planContent;
    private Integer planAmount;

    public UsePlan toEntity(Integer campaignNo) { // Campaign 엔티티의 ID 타입에 맞춤
        return UsePlan.builder()
                .planContent(this.planContent)
                .planAmount(this.planAmount)
                .campaignNo(campaignNo)
                .build();
    }
}

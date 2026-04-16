package com.merge.final_project.campaign.campaigns.dto;

import com.merge.final_project.campaign.campaigns.CampaignCategory;
import com.merge.final_project.campaign.campaigns.entity.Campaign;
import com.merge.final_project.campaign.useplan.dto.UsePlanRequestDTO;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class CampaignRequestDTO {
    private String title;
    private String description;
    private String imagePath;
    private CampaignCategory category;
    private Long targetAmount;
    private String entryCode;

    private LocalDateTime startAt;
    private LocalDateTime endAt;
    private LocalDateTime usageStartAt;
    private LocalDateTime usageEndAt;

    private List<UsePlanRequestDTO> usePlans;
    private List<Long> deletedDetailImageNos;

    public Campaign toEntity() {
        return Campaign.builder()
                .title(this.title)
                .description(this.description)
                .category(this.category)
                .targetAmount(this.targetAmount)
                .startAt(this.startAt)
                .endAt(this.endAt)
                .usageStartAt(this.usageStartAt)
                .usageEndAt(this.usageEndAt)
                .build();
    }
}

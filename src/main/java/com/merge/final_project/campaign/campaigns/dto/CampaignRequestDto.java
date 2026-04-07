package com.merge.final_project.campaign.campaigns.dto;

import com.merge.final_project.campaign.campaigns.entity.Campaign;
import com.merge.final_project.campaign.useplan.dto.UsePlanRequestDTO;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

// React가 보낸 JSON ?�이????객체??매핑
@Data
public class CampaignRequestDTO {
    private String title;
    private String description;
    private String imagePath;
    private String category;
    private Long targetAmount;

    private LocalDateTime startAt;
    private LocalDateTime endAt;
    private LocalDateTime usageStartAt;
    private LocalDateTime usageEndAt;

    private List<UsePlanRequestDTO> usePlans;

    // DTO ?�이??바탕?�로 DB ?�??객체 ?�성
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


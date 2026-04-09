package com.merge.final_project.campaign.campaigns.dto;

import com.merge.final_project.campaign.campaigns.entity.Campaign;
import com.merge.final_project.campaign.useplan.dto.UsePlanRequestDto;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class CampaignResponseDto {
    private String title;
    private String description;
    private String imagePath;
    private String category;
    private Long targetAmount;

    private LocalDateTime startAt;
    private LocalDateTime endAt;
    private LocalDateTime usageStartAt;
    private LocalDateTime usageEndAt;

    private List<UsePlanRequestDto> usePlans;

    // 서비스 계층에서 DB 저장 시 사용
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

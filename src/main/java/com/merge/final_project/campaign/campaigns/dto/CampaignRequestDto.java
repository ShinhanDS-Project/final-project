package com.merge.final_project.campaign.campaigns.dto;

import com.merge.final_project.campaign.campaigns.entity.Campaign;
import com.merge.final_project.campaign.useplan.dto.UsePlanRequestDto;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

// React가 보낸 JSON 데이터 이 객체에 매핑
@Data
public class CampaignRequestDto {
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

    // DTO 데이터 바탕으로 DB 저장 객체 생성
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

package com.merge.final_project.org.dto;

import com.merge.final_project.campaign.campaigns.ApprovalStatus;
import com.merge.final_project.campaign.campaigns.CampaignStatus;
import com.merge.final_project.campaign.campaigns.entity.Campaign;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

@Getter
@Builder
public class FoundationMyCampaignDTO {

    private Long campaignNo;
    private String title;
    private String imagePath;
    private String category;
    private Long targetAmount;
    private BigDecimal currentAmount;
    private int progressPercent;        // 목표 달성률 (0~100)
    private CampaignStatus campaignStatus;
    private ApprovalStatus approvalStatus;
    private String rejectReason;
    private LocalDateTime startAt;
    private LocalDateTime endAt;
    private LocalDateTime createdAt;

    public static FoundationMyCampaignDTO from(Campaign campaign) {
        int progress = 0;
        if (campaign.getTargetAmount() != null && campaign.getTargetAmount() > 0
                && campaign.getCurrentAmount() != null) {
            progress = campaign.getCurrentAmount()
                    .multiply(BigDecimal.valueOf(100))
                    .divide(BigDecimal.valueOf(campaign.getTargetAmount()), RoundingMode.DOWN)
                    .min(BigDecimal.valueOf(100))
                    .intValue();
        }

        return FoundationMyCampaignDTO.builder()
                .campaignNo(campaign.getCampaignNo())
                .title(campaign.getTitle())
                .imagePath(campaign.getImagePath())
                .category(campaign.getCategory() == null ? null : campaign.getCategory().getLabel())
                .targetAmount(campaign.getTargetAmount())
                .currentAmount(campaign.getCurrentAmount())
                .progressPercent(progress)
                .campaignStatus(campaign.getCampaignStatus())
                .approvalStatus(campaign.getApprovalStatus())
                .rejectReason(campaign.getRejectReason())
                .startAt(campaign.getStartAt())
                .endAt(campaign.getEndAt())
                .createdAt(campaign.getCreatedAt())
                .build();
    }
}

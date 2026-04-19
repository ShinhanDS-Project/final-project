package com.merge.final_project.admin.dto;

import com.merge.final_project.campaign.campaigns.ApprovalStatus;
import com.merge.final_project.campaign.campaigns.CampaignCategory;
import com.merge.final_project.campaign.campaigns.CampaignStatus;
import com.merge.final_project.campaign.campaigns.entity.Campaign;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
public class AdminCampaignDTO {
    private Long campaignNo;
    private String title;
    private String description;
    private String imagePath;
    private LocalDateTime startAt;
    private LocalDateTime endAt;
    private LocalDateTime usageStartAt;
    private LocalDateTime usageEndAt;
    private Long targetAmount;
    private BigDecimal currentAmount;
    private LocalDateTime achievedAt;
    private CampaignCategory category;
    private ApprovalStatus approvalStatus;
    private LocalDateTime approvedAt;
    private LocalDateTime updatedAt;
    private CampaignStatus campaignStatus;
    private Long foundationNo;
    private Long walletNo;
    private Long beneficiaryNo;
    private String rejectReason;
    private LocalDateTime createdAt;

    public static AdminCampaignDTO from(Campaign c) {
        return AdminCampaignDTO.builder()
                .campaignNo(c.getCampaignNo())
                .title(c.getTitle())
                .description(c.getDescription())
                .imagePath(c.getImagePath())
                .startAt(c.getStartAt())
                .endAt(c.getEndAt())
                .usageStartAt(c.getUsageStartAt())
                .usageEndAt(c.getUsageEndAt())
                .targetAmount(c.getTargetAmount())
                .currentAmount(c.getCurrentAmount())
                .achievedAt(c.getAchievedAt())
                .category(c.getCategory())
                .approvalStatus(c.getApprovalStatus())
                .approvedAt(c.getApprovedAt())
                .updatedAt(c.getUpdatedAt())
                .campaignStatus(c.getCampaignStatus())
                .foundationNo(c.getFoundationNo())
                .walletNo(c.getWalletNo())
                .beneficiaryNo(c.getBeneficiaryNo())
                .rejectReason(c.getRejectReason())
                .createdAt(c.getCreatedAt())
                .build();
    }

    // [가빈] Image 테이블에서 조회한 이미지 경로를 우선 사용하는 오버로드
    public static AdminCampaignDTO from(Campaign c, String imagePath) {
        return AdminCampaignDTO.builder()
                .campaignNo(c.getCampaignNo())
                .title(c.getTitle())
                .description(c.getDescription())
                .imagePath(imagePath)
                .startAt(c.getStartAt())
                .endAt(c.getEndAt())
                .usageStartAt(c.getUsageStartAt())
                .usageEndAt(c.getUsageEndAt())
                .targetAmount(c.getTargetAmount())
                .currentAmount(c.getCurrentAmount())
                .achievedAt(c.getAchievedAt())
                .category(c.getCategory())
                .approvalStatus(c.getApprovalStatus())
                .approvedAt(c.getApprovedAt())
                .updatedAt(c.getUpdatedAt())
                .campaignStatus(c.getCampaignStatus())
                .foundationNo(c.getFoundationNo())
                .walletNo(c.getWalletNo())
                .beneficiaryNo(c.getBeneficiaryNo())
                .rejectReason(c.getRejectReason())
                .createdAt(c.getCreatedAt())
                .build();
    }
}

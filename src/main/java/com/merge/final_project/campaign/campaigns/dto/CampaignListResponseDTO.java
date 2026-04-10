package com.merge.final_project.campaign.campaigns.dto;

import lombok.AllArgsConstructor;
import com.merge.final_project.campaign.campaigns.ApprovalStatus;
import com.merge.final_project.campaign.campaigns.entity.Campaign;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CampaignListResponseDTO {  //[가빈] 관리자 측에서 캠페인 목록 조회해오려고 만든 DTO입니다.

    private Long campaignNo;
    private String imagePath;
    private String title;
    private String foundationName;
    private Long targetAmount;
    private BigDecimal currentAmount;
    private String category;
    private LocalDateTime endAt;
    private Long foundationNo;
    private ApprovalStatus approvalStatus;
    private String rejectReason;
    private LocalDateTime createdAt;

    public static CampaignListResponseDTO from(Campaign campaign) {
        return CampaignListResponseDTO.builder()
                .campaignNo(campaign.getCampaignNo())
                .title(campaign.getTitle())
                .category(campaign.getCategory())
                .targetAmount(campaign.getTargetAmount())
                .foundationNo(campaign.getFoundationNo())
                .approvalStatus(campaign.getApprovalStatus())
                .rejectReason(campaign.getRejectReason())
                .createdAt(campaign.getCreatedAt())
                .build();
    }
}

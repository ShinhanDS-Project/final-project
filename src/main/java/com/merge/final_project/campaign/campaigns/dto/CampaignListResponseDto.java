package com.merge.final_project.campaign.campaigns.dto;

import com.merge.final_project.campaign.campaigns.ApprovalStatus;
import com.merge.final_project.campaign.campaigns.entity.Campaign;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class CampaignListResponseDto {  //[가빈] 관리자 측에서 캠페인 목록 조회해오려고 만든 DTO입니다.

    private Long campaignNo;
    private String title;
    private String category;
    private Long targetAmount;
    private Long foundationNo;
    private ApprovalStatus approvalStatus;
    private String rejectReason;
    private LocalDateTime createdAt;

    public static CampaignListResponseDto from(Campaign campaign) {
        return CampaignListResponseDto.builder()
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

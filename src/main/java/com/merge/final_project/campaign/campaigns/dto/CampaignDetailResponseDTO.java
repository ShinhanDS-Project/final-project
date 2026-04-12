package com.merge.final_project.campaign.campaigns.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class CampaignDetailResponseDTO {
    private Long campaignNo;
    private String title;
    private String description;
    private String category;
    private String approvalStatus;
    private String campaignStatus;
    private String campaignStatusLabel;
    private String historyTitle;
    private String historyDescription;
    private Long targetAmount;
    private Long currentAmount;
    private Integer progressPercent;
    private Long remainingAmount;
    private Long daysLeft;
    private LocalDateTime startAt;
    private LocalDateTime endAt;
    private LocalDateTime usageStartAt;
    private LocalDateTime usageEndAt;
    private String walletAddress;
    private String representativeImagePath;
    private List<String> detailImagePaths;
    private FoundationSummary foundation;
    private List<UsePlanSummary> usePlans;

    @Getter
    @Builder
    public static class FoundationSummary {
        private Long foundationNo;
        private String foundationName;
        private String description;
        private String profilePath;
    }

    @Getter
    @Builder
    public static class UsePlanSummary {
        private Long usePlanNo;
        private String planContent;
        private Long planAmount;
    }
}

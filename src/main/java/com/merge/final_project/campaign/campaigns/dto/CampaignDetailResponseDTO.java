package com.merge.final_project.campaign.campaigns.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/* [해석] 캠페인 상세 조회 시 필요한 모든 데이터를 한 번에 담아서 전달하는 응답용 객체 */
@Getter
@Builder
public class CampaignDetailResponseDTO {
    private Long campaignNo;
    private String title;
    private String description;
    private String category;
    private String approvalStatus;
    private String campaignStatus;
    private String campaignStatusLabel; // 캠페인 상태를 화면에 보여줄 한글 레이블
    private String historyTitle;
    private String historyDescription;
    private Long targetAmount;
    private BigDecimal currentAmount;
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

    /* 기부 단체 요약 정보를 담는 내부 DTO */
    @Getter
    @Builder
    public static class FoundationSummary {
        private Long foundationNo;
        private String foundationName;
        private String description;
        private String profilePath;
    }

    /* 기부금이 어디에 사용될지 정의한 실행 계획 요약 DTO */
    @Getter
    @Builder
    public static class UsePlanSummary {
        private Long usePlanNo;
        private String planContent; // 사용 계획 내용
        private Long planAmount; // 계획된 사용 금액
    }
}
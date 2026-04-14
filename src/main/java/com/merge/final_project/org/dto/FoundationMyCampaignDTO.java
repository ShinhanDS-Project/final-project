package com.merge.final_project.org.dto;

import com.merge.final_project.campaign.campaigns.ApprovalStatus;
import com.merge.final_project.campaign.campaigns.CampaignStatus;
import com.merge.final_project.campaign.campaigns.entity.Campaign;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

@Schema(description = "기부단체 마이페이지 캠페인 DTO")
@Getter
@Builder
public class FoundationMyCampaignDTO {

    @Schema(description = "캠페인 번호", example = "1")
    private Long campaignNo;

    @Schema(description = "캠페인 제목", example = "어린이 급식 지원 캠페인")
    private String title;

    @Schema(description = "캠페인 대표 이미지 URL", example = "https://bucket.s3.region.amazonaws.com/campaigns/1.jpg")
    private String imagePath;

    @Schema(description = "캠페인 카테고리 한글명", example = "아동·청소년")
    private String category;

    @Schema(description = "목표 모금액 (원)", example = "5000000")
    private Long targetAmount;

    @Schema(description = "현재 모금액 (원)", example = "3200000")
    private BigDecimal currentAmount;

    @Schema(description = "목표 달성률 (0~100 %)", example = "64")
    private int progressPercent;

    @Schema(description = "캠페인 진행 상태 (PENDING, RECRUITING, ACTIVE, ENDED, SETTLED, COMPLETED, CANCELLED)", example = "ACTIVE")
    private CampaignStatus campaignStatus;

    @Schema(description = "관리자 승인 상태 (PENDING, APPROVED, REJECTED)", example = "APPROVED")
    private ApprovalStatus approvalStatus;

    @Schema(description = "반려 사유 (반려된 경우에만 값 존재)", example = "캠페인 내용이 부적절합니다.")
    private String rejectReason;

    @Schema(description = "모집 시작 일시", example = "2024-02-01T00:00:00")
    private LocalDateTime startAt;

    @Schema(description = "모집 종료 일시", example = "2024-03-31T23:59:59")
    private LocalDateTime endAt;

    @Schema(description = "캠페인 등록 일시", example = "2024-01-20T10:00:00")
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

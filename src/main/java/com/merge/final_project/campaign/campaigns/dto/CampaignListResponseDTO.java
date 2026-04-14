package com.merge.final_project.campaign.campaigns.dto;

import lombok.AllArgsConstructor;
import com.merge.final_project.campaign.campaigns.ApprovalStatus;
import com.merge.final_project.campaign.campaigns.entity.Campaign;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "캠페인 목록 응답 DTO")
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CampaignListResponseDTO {

    @Schema(description = "캠페인 번호", example = "1")
    private Long campaignNo;

    @Schema(description = "캠페인 대표 이미지 URL", example = "https://bucket.s3.region.amazonaws.com/campaigns/1.jpg")
    private String imagePath;

    @Schema(description = "캠페인 제목", example = "어린이 급식 지원 캠페인")
    private String title;

    @Schema(description = "기부단체명", example = "초록우산 어린이재단")
    private String foundationName;

    @Schema(description = "목표 모금액 (원)", example = "5000000")
    private Long targetAmount;

    @Schema(description = "현재 모금액 (원)", example = "3200000")
    private BigDecimal currentAmount;

    @Schema(description = "카테고리 한글명", example = "아동·청소년")
    private String category;

    @Schema(description = "모집 종료 일시", example = "2024-03-31T23:59:59")
    private LocalDateTime endAt;

    @Schema(description = "기부단체 번호", example = "1")
    private Long foundationNo;

    @Schema(description = "관리자 승인 상태 (PENDING, APPROVED, REJECTED)", example = "APPROVED")
    private ApprovalStatus approvalStatus;

    @Schema(description = "반려 사유 (반려된 경우에만 값 존재)", example = "캠페인 내용이 부적절합니다.")
    private String rejectReason;

    @Schema(description = "캠페인 등록 일시", example = "2024-01-20T10:00:00")
    private LocalDateTime createdAt;

    public static CampaignListResponseDTO from(Campaign campaign) {
        return CampaignListResponseDTO.builder()
                .campaignNo(campaign.getCampaignNo())
                .title(campaign.getTitle())
                .category(campaign.getCategory() == null ? null : campaign.getCategory().getLabel())
                .targetAmount(campaign.getTargetAmount())
                .foundationNo(campaign.getFoundationNo())
                .approvalStatus(campaign.getApprovalStatus())
                .rejectReason(campaign.getRejectReason())
                .createdAt(campaign.getCreatedAt())
                .build();
    }
}

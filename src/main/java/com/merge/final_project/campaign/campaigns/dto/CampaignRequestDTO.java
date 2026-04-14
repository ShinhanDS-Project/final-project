package com.merge.final_project.campaign.campaigns.dto;

import com.merge.final_project.campaign.campaigns.CampaignCategory;
import com.merge.final_project.campaign.campaigns.entity.Campaign;
import com.merge.final_project.campaign.useplan.dto.UsePlanRequestDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "캠페인 등록 요청 DTO")
@Data
public class CampaignRequestDTO {

    @Schema(description = "캠페인 제목", example = "어린이 급식 지원 캠페인", requiredMode = Schema.RequiredMode.REQUIRED)
    private String title;

    @Schema(description = "캠페인 상세 설명", example = "저소득 아이들에게 급식을 지원합니다.")
    private String description;

    @Schema(description = "대표 이미지 경로 (서버 업로드 후 경로, RequestPart imageFile로 대체 사용)", example = "campaigns/1.jpg")
    private String imagePath;

    @Schema(description = "캠페인 카테고리 (CHILD, ELDERLY, DISABLED, ANIMAL, ENVIRONMENT 등)", example = "CHILD", requiredMode = Schema.RequiredMode.REQUIRED)
    private CampaignCategory category;

    @Schema(description = "목표 모금액 (원)", example = "5000000", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long targetAmount;

    @Schema(description = "수혜자 엔트리 코드", example = "ENT-20240101-001", requiredMode = Schema.RequiredMode.REQUIRED)
    private String entryCode;

    @Schema(description = "모집 시작 일시", example = "2024-02-01T00:00:00", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime startAt;

    @Schema(description = "모집 종료 일시", example = "2024-03-31T23:59:59", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime endAt;

    @Schema(description = "기부금 사용 시작 일시", example = "2024-04-01T00:00:00", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime usageStartAt;

    @Schema(description = "기부금 사용 종료 일시", example = "2024-06-30T23:59:59", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime usageEndAt;

    @Schema(description = "지출 계획 목록")
    private List<UsePlanRequestDTO> usePlans;

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

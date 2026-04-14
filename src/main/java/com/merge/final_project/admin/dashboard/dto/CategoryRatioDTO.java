package com.merge.final_project.admin.dashboard.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Schema(description = "카테고리별 캠페인 비중 DTO")
@Getter
@Builder
public class CategoryRatioDTO {

    @Schema(description = "카테고리 enum 이름", example = "CHILD")
    private String category;

    @Schema(description = "카테고리 한글 표시명", example = "아동·청소년")
    private String categoryLabel;

    @Schema(description = "해당 카테고리 캠페인 수", example = "12")
    private long campaignCount;

    @Schema(description = "해당 카테고리 총 기부금 (원)", example = "4500000")
    private BigDecimal donationAmount;
}

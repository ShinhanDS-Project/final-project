package com.merge.final_project.admin.dashboard.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Schema(description = "일별 기부금 추이 DTO")
@Getter
@Builder
public class DonationTrendDTO {

    @Schema(description = "날짜 (yyyy-MM-dd 형식)", example = "2024-01-15")
    private String date;

    @Schema(description = "해당 날짜 기부 총액 (원)", example = "350000")
    private BigDecimal amount;
}

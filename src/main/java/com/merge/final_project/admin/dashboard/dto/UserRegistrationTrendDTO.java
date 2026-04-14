package com.merge.final_project.admin.dashboard.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Schema(description = "일별 사용자 가입 추이 DTO")
@Getter
@Builder
public class UserRegistrationTrendDTO {

    @Schema(description = "날짜 (yyyy-MM-dd 형식)", example = "2024-01-15")
    private String date;

    @Schema(description = "해당 날짜 신규 가입자 수", example = "35")
    private long count;
}

package com.merge.final_project.org.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Schema(description = "기부단체 회원정보 수정 요청 DTO")
@Getter
@NoArgsConstructor
public class FoundationUpdateRequestDTO {

    @Schema(description = "단체 소개 및 활동 설명", example = "어린이의 행복한 미래를 위해 활동합니다.", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank
    private String description;

    @Schema(description = "연락처", example = "02-1234-5678", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank
    private String contactPhone;

    @Schema(description = "기부금 수령 계좌번호", example = "123-456-789012", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank
    private String account;

    @Schema(description = "은행명", example = "국민은행", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank
    private String bankName;

    @Schema(description = "플랫폼 수수료율 (0.00 ~ 1.00)", example = "0.05", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull
    @DecimalMin("0.00")
    @DecimalMax("1.00")
    private BigDecimal feeRate;
}

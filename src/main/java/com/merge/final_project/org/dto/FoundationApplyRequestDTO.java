package com.merge.final_project.org.dto;

import com.merge.final_project.org.FoundationType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Schema(description = "기부단체 가입 신청 요청 DTO")
@Getter
@NoArgsConstructor
public class FoundationApplyRequestDTO {

    @Schema(description = "기부단체 이메일 (로그인 아이디로 사용)", example = "foundation@example.com", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "이메일을 입력해주세요.")
    @Email(message = "올바른 이메일 형식이 아닙니다.")
    private String foundationEmail;

    @Schema(description = "단체명", example = "초록우산 어린이재단", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "단체명을 입력해주세요.")
    private String foundationName;

    @Schema(description = "단체 유형 (CHILD, ELDERLY, DISABLED, ANIMAL, ENVIRONMENT, OTHER 등)", example = "CHILD", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "단체 유형을 선택해주세요.")
    private FoundationType foundationType;

    @Schema(description = "대표자명", example = "홍길동", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "대표자명을 입력해주세요.")
    private String representativeName;

    @Schema(description = "사업자등록번호 (하이픈 제외, 10자리)", example = "1234567890", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "사업자등록번호를 입력해주세요.")
    private String businessRegistrationNumber;

    @Schema(description = "연락처", example = "02-1234-5678", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "연락처를 입력해주세요.")
    private String contactPhone;

    @Schema(description = "단체 소개 및 활동 설명", example = "어린이의 행복한 미래를 위해 활동합니다.")
    private String description;

    @Schema(description = "기부금 수령 계좌번호", example = "123-456-789012")
    private String account;

    @Schema(description = "은행명", example = "국민은행")
    private String bankName;

    @Schema(description = "플랫폼 수수료율 (0.00 ~ 1.00, 예: 0.05 = 5%)", example = "0.05", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "수수료율을 입력해 주세요.")
    @DecimalMin("0.00")
    @DecimalMax("1.00")
    private BigDecimal feeRate;
}

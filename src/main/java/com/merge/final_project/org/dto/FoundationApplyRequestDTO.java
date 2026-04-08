package com.merge.final_project.org.dto;

import com.merge.final_project.org.FoundationType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor  //Jackson이 JSON을 역직렬화할 때 기본 생성자가 필요. 따라서 빌드만 있으면 안 되고 기본 생성자가 필요.
public class FoundationApplyRequestDTO {

    @NotBlank(message = "이메일을 입력해주세요.")
    @Email(message = "올바른 이메일 형식이 아닙니다.")
    private String foundationEmail;

    @NotBlank(message = "단체명을 입력해주세요.")
    private String foundationName;

    @NotNull(message = "단체 유형을 선택해주세요.")
    private FoundationType foundationType;

    @NotBlank(message = "대표자명을 입력해주세요.")
    private String representativeName;

    @NotBlank(message = "사업자등록번호를 입력해주세요.")
    private String businessRegistrationNumber;

    @NotBlank(message = "연락처를 입력해주세요.")
    private String contactPhone;

    private String description;

    private String account;

    private String bankName;

    private BigDecimal feeRate;


}

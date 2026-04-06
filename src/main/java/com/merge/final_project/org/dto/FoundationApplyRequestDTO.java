package com.merge.final_project.org.dto;

import com.merge.final_project.org.FoundationType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
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


}

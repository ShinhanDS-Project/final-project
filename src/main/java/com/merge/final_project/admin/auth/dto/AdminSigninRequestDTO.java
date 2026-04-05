package com.merge.final_project.admin.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminSigninRequestDTO {

    @NotBlank(message = "아이디를 입력해주세요.")
    private String adminId;

    @NotBlank(message = "비밀번호를 입력해주세요.")
    private String password;
}

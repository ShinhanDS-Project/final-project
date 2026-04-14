package com.merge.final_project.admin.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Schema(description = "관리자 로그인 요청 DTO")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminSigninRequestDTO {

    @Schema(description = "관리자 아이디", example = "admin01", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "아이디를 입력해주세요.")
    private String adminId;

    @Schema(description = "비밀번호", example = "password123!", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "비밀번호를 입력해주세요.")
    private String password;
}

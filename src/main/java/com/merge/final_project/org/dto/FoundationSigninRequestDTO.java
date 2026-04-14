package com.merge.final_project.org.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Schema(description = "기부단체 로그인 요청 DTO")
@Getter
@NoArgsConstructor
public class FoundationSigninRequestDTO {

    @Schema(description = "기부단체 이메일", example = "foundation@example.com", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank
    private String email;

    @Schema(description = "비밀번호", example = "password123!", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank
    private String password;
}

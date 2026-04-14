package com.merge.final_project.org.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Schema(description = "기부단체 비밀번호 변경 요청 DTO")
@Getter
@NoArgsConstructor
public class FoundationPasswordUpdateRequestDTO {

    @Schema(description = "현재 비밀번호", example = "currentPw123!", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank
    private String currentPassword;

    @Schema(description = "새 비밀번호", example = "newPw456@", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank
    private String newPassword;
}

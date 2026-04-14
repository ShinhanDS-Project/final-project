package com.merge.final_project.admin.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Schema(description = "관리자 로그인 응답 DTO")
@Getter
@AllArgsConstructor
@Builder
public class AdminSigninResponseDTO {

    @Schema(description = "JWT 액세스 토큰", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String accessToken;

    @Schema(description = "토큰 타입", example = "Bearer")
    private String tokenType;

    @Schema(description = "관리자 아이디", example = "admin01")
    private String adminId;

    @Schema(description = "관리자 이름", example = "홍길동")
    private String name;

    @Schema(description = "관리자 역할", example = "ROLE_ADMIN")
    private String adminRole;
}

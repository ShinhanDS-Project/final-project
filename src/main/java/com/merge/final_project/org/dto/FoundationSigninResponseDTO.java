package com.merge.final_project.org.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Schema(description = "기부단체 로그인 응답 DTO")
@Getter
@Builder
public class FoundationSigninResponseDTO {

    @Schema(description = "JWT 액세스 토큰", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String accessToken;

    @Schema(description = "토큰 타입", example = "Bearer")
    private String tokenType;

    @Schema(description = "기부단체 번호", example = "1")
    private Long foundationNo;

    @Schema(description = "단체명", example = "초록우산 어린이재단")
    private String foundationName;

    @Schema(description = "기부단체 이메일", example = "foundation@example.com")
    private String email;
}

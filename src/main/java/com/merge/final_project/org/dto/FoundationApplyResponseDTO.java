package com.merge.final_project.org.dto;

import com.merge.final_project.org.ReviewStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Schema(description = "기부단체 가입 신청 응답 DTO")
@Getter
@Builder
public class FoundationApplyResponseDTO {

    @Schema(description = "기부단체 번호", example = "1")
    private Long foundationNo;

    @Schema(description = "기부단체 이메일", example = "foundation@example.com")
    private String foundationEmail;

    @Schema(description = "단체명", example = "초록우산 어린이재단")
    private String foundationName;

    @Schema(description = "대표자명", example = "홍길동")
    private String representativeName;

    @Schema(description = "심사 상태 (PENDING: 대기, APPROVED: 승인, ILLEGAL: 반려)", example = "PENDING")
    private ReviewStatus reviewStatus;
}

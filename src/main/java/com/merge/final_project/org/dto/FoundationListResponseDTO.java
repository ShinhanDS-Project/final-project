package com.merge.final_project.org.dto;

import com.merge.final_project.org.AccountStatus;
import com.merge.final_project.org.Foundation;
import com.merge.final_project.org.FoundationType;
import com.merge.final_project.org.ReviewStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Schema(description = "기부단체 목록 응답 DTO")
@Getter
@Builder
public class FoundationListResponseDTO {

    @Schema(description = "기부단체 번호", example = "1")
    private Long foundationNo;

    @Schema(description = "단체명", example = "초록우산 어린이재단")
    private String foundationName;

    @Schema(description = "기부단체 이메일", example = "foundation@example.com")
    private String foundationEmail;

    @Schema(description = "대표자명", example = "홍길동")
    private String representativeName;

    @Schema(description = "단체 유형", example = "CHILD")
    private FoundationType foundationType;

    @Schema(description = "심사 상태 (PENDING, APPROVED, ILLEGAL)", example = "APPROVED")
    private ReviewStatus reviewStatus;

    @Schema(description = "계정 상태 (PRE_REGISTERED, ACTIVE, INACTIVE)", example = "ACTIVE")
    private AccountStatus accountStatus;

    @Schema(description = "가입 신청 일시", example = "2024-01-10T09:00:00")
    private LocalDateTime createdAt;

    public static FoundationListResponseDTO from(Foundation foundation) {
        return FoundationListResponseDTO.builder()
                .foundationNo(foundation.getFoundationNo())
                .foundationName(foundation.getFoundationName())
                .foundationEmail(foundation.getFoundationEmail())
                .representativeName(foundation.getRepresentativeName())
                .foundationType(foundation.getFoundationType())
                .reviewStatus(foundation.getReviewStatus())
                .accountStatus(foundation.getAccountStatus())
                .createdAt(foundation.getCreatedAt())
                .build();
    }
}

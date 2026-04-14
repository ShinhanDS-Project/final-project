package com.merge.final_project.org.dto;

import com.merge.final_project.org.AccountStatus;
import com.merge.final_project.org.Foundation;
import com.merge.final_project.org.FoundationType;
import com.merge.final_project.org.ReviewStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Schema(description = "기부단체 상세 응답 DTO")
@Getter
@Builder
public class FoundationDetailResponseDTO {

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

    @Schema(description = "사업자등록번호", example = "1234567890")
    private String businessRegistrationNumber;

    @Schema(description = "연락처", example = "02-1234-5678")
    private String contactPhone;

    @Schema(description = "단체 소개 및 활동 설명", example = "어린이의 행복한 미래를 위해 활동합니다.")
    private String description;

    @Schema(description = "기부금 수령 계좌번호", example = "123-456-789012")
    private String account;

    @Schema(description = "심사 상태 (PENDING: 가입 심사 중, APPROVED: 승인, ILLEGAL: 반려)", example = "APPROVED")
    private ReviewStatus reviewStatus;

    @Schema(description = "계정 상태 (PRE_REGISTERED: 가입 전, ACTIVE: 활성, INACTIVE: 비활성)", example = "ACTIVE")
    private AccountStatus accountStatus;

    @Schema(description = "프로필 이미지 URL (null이면 기본 이미지 사용)", example = "https://bucket.s3.region.amazonaws.com/profile/1.jpg")
    private String profilePath;

    @Schema(description = "반려 사유 (반려된 경우에만 값 존재)", example = "불법 단체로 확인되었습니다.")
    private String rejectReason;

    @Schema(description = "가입 신청 일시", example = "2024-01-10T09:00:00")
    private LocalDateTime createdAt;

    @Schema(description = "최종 수정 일시", example = "2024-01-15T14:30:00")
    private LocalDateTime updatedAt;

    public static FoundationDetailResponseDTO from(Foundation foundation) {
        return FoundationDetailResponseDTO.builder()
                .foundationNo(foundation.getFoundationNo())
                .foundationName(foundation.getFoundationName())
                .foundationEmail(foundation.getFoundationEmail())
                .representativeName(foundation.getRepresentativeName())
                .foundationType(foundation.getFoundationType())
                .businessRegistrationNumber(foundation.getBusinessRegistrationNumber())
                .contactPhone(foundation.getContactPhone())
                .description(foundation.getDescription())
                .account(foundation.getAccount())
                .profilePath(foundation.getProfilePath())
                .reviewStatus(foundation.getReviewStatus())
                .accountStatus(foundation.getAccountStatus())
                .rejectReason(foundation.getRejectReason())
                .createdAt(foundation.getCreatedAt())
                .updatedAt(foundation.getUpdatedAt())
                .build();
    }

}

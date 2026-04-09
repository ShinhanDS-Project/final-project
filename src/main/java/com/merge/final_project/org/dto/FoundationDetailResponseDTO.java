package com.merge.final_project.org.dto;

import com.merge.final_project.org.AccountStatus;
import com.merge.final_project.org.Foundation;
import com.merge.final_project.org.FoundationType;
import com.merge.final_project.org.ReviewStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class FoundationDetailResponseDTO {

    private Long foundationNo;
    private String foundationName;
    private String foundationEmail;
    private String representativeName;
    private FoundationType foundationType;
    private String businessRegistrationNumber;
    private String contactPhone;
    private String description;
    private String account;
    private ReviewStatus reviewStatus;   // PENDING / ILLEGAL / APPROVED / REJECTED -> 가입 상태
    private AccountStatus accountStatus;  // PRE_REGISTERED / ACTIVE / INACTIVE    -> 기부단체 계정 상태
    private String rejectReason;
    private LocalDateTime createdAt;
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
                .reviewStatus(foundation.getReviewStatus())
                .accountStatus(foundation.getAccountStatus())
                .rejectReason(foundation.getRejectReason())
                .createdAt(foundation.getCreatedAt())
                .updatedAt(foundation.getUpdatedAt())
                .build();
    }

}

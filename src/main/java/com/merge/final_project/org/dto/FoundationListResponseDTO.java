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
public class FoundationListResponseDTO {

    private Long foundationNo;
    private String foundationName;
    private String foundationEmail;
    private String representativeName;
    private FoundationType foundationType;
    private ReviewStatus reviewStatus;
    private AccountStatus accountStatus;
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

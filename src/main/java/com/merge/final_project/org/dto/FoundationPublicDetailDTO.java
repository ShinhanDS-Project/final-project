package com.merge.final_project.org.dto;

import com.merge.final_project.org.Foundation;
import com.merge.final_project.org.FoundationType;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
public class FoundationPublicDetailDTO {

    private Long foundationNo;
    private String foundationName;
    private String representativeName;
    private FoundationType foundationType;
    private String businessRegistrationNumber;
    private String contactPhone;
    private String description;
    private String profilePath;
    private BigDecimal feeRate;
    private String bankName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static FoundationPublicDetailDTO from(Foundation foundation) {
        return FoundationPublicDetailDTO.builder()
                .foundationNo(foundation.getFoundationNo())
                .foundationName(foundation.getFoundationName())
                .representativeName(foundation.getRepresentativeName())
                .foundationType(foundation.getFoundationType())
                .businessRegistrationNumber(foundation.getBusinessRegistrationNumber())
                .contactPhone(foundation.getContactPhone())
                .description(foundation.getDescription())
                .profilePath(foundation.getProfilePath())
                .feeRate(foundation.getFeeRate())
                .bankName(foundation.getBankName())
                .createdAt(foundation.getCreatedAt())
                .updatedAt(foundation.getUpdatedAt())
                .build();
    }
}

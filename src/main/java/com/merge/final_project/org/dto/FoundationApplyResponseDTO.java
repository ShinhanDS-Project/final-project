package com.merge.final_project.org.dto;


import com.merge.final_project.org.ReviewStatus;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class FoundationApplyResponseDTO {

    private Long foundationNo;
    private String foundationEmail;
    private String foundationName;
    private String representativeName;
    private ReviewStatus reviewStatus;
}

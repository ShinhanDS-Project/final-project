package com.merge.final_project.org.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class FoundationSigninResponseDTO {

    private String accessToken;
    private String tokenType;
    private Long foundationNo;
    private String foundationName;
    private String email;
}

package com.merge.final_project.admin.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class AdminSigninResponseDTO {
    private String accessToken;
    private String tokenType;
    private String adminId;
    private String name;
    private String adminRole;
}

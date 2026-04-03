package com.merge.final_project.admin.admins.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class AdminSigninResponseDTO {
    private String accessToken;
}

package com.merge.final_project.admin.admins.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminSigninRequestDTO {
    private String adminId;
    private String password;
}

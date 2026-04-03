package com.merge.final_project.admin.admins.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class AdminSigninRequestDTO {
    private String adminId;
    private String password;
}

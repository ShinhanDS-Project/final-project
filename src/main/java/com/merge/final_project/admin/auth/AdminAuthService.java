package com.merge.final_project.admin.auth;

import com.merge.final_project.admin.auth.dto.AdminSigninRequestDTO;
import com.merge.final_project.admin.auth.dto.AdminSigninResponseDTO;

public interface AdminAuthService {
    AdminSigninResponseDTO login(AdminSigninRequestDTO requestDTO);

    void logout(String bearerToken);
}

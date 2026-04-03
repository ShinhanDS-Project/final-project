package com.merge.final_project.admin.admins;

import com.merge.final_project.admin.admins.dto.AdminSigninRequestDTO;
import com.merge.final_project.admin.admins.dto.AdminSigninResponseDTO;

public interface AdminAuthService {
    public AdminSigninResponseDTO login (AdminSigninRequestDTO requestDTO);
}

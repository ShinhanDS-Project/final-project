package com.merge.final_project.admin.admins;

import com.merge.final_project.admin.admins.dto.AdminSigninRequestDTO;
import com.merge.final_project.admin.admins.dto.AdminSigninResponseDTO;
import com.merge.final_project.global.exception.ErrorCode;
import com.merge.final_project.global.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AdminAuthServiceImpl implements  AdminAuthService{
    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public AdminSigninResponseDTO login(AdminSigninRequestDTO requestDTO) {

        Optional<Admin> admin = adminRepository.findByAdminId(requestDTO.getAdminId())
                .orElseThrow(() -> new BusinessException(ErrorCode.ADMIN_NOT_FOUND));

        if (!passwordEncoder.matches(requestDTO.getPassword(), admin.get().getPassword())) {
            throw new BusinessException(ErrorCode.INVALID_PASSWORD);
        }

        return null;
    }
}

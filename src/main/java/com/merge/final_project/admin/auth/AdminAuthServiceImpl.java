package com.merge.final_project.admin.auth;

import com.merge.final_project.admin.Admin;
import com.merge.final_project.admin.AdminRepository;
import com.merge.final_project.admin.auth.dto.AdminSigninRequestDTO;
import com.merge.final_project.admin.auth.dto.AdminSigninResponseDTO;
import com.merge.final_project.global.exceptions.ErrorCode;
import com.merge.final_project.global.exceptions.BusinessException;
import com.merge.final_project.global.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminAuthServiceImpl implements  AdminAuthService{
    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public AdminSigninResponseDTO login(AdminSigninRequestDTO requestDTO) {

        Admin admin = adminRepository.findByAdminId(requestDTO.getAdminId())
                .orElseThrow(() -> new BusinessException(ErrorCode.ADMIN_NOT_FOUND));

        if (!passwordEncoder.matches(requestDTO.getPassword(), admin.getPassword())) {
            throw new BusinessException(ErrorCode.INVALID_PASSWORD);
        }

        String accessToken = jwtTokenProvider.createAdminAccessToken(admin.getAdminId(), admin.getAdminRole());

        return AdminSigninResponseDTO.builder()
                .accessToken(accessToken)
                .tokenType("Bearer")
                .adminId(admin.getAdminId())
                .name(admin.getName())
                .adminRole(admin.getAdminRole())
                .build();
    }

    @Override
    public void logout(String bearerToken) {
        String token = bearerToken.substring(7);
        //추후 시간 남으면 블랙리스트 구현해서 체크하는 서비스로 확장 예정
    }
}

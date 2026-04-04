package com.merge.final_project.admin;

import com.merge.final_project.admin.admins.Admin;
import com.merge.final_project.admin.admins.AdminAuthServiceImpl;
import com.merge.final_project.admin.admins.AdminRepository;
import com.merge.final_project.admin.admins.dto.AdminSigninRequestDTO;
import com.merge.final_project.admin.admins.dto.AdminSigninResponseDTO;
import com.merge.final_project.global.exceptions.BusinessException;
import com.merge.final_project.global.exceptions.ErrorCode;
import com.merge.final_project.global.jwt.JwtTokenProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminServiceTests {

    @InjectMocks
    private AdminAuthServiceImpl adminAuthService;

    @Mock
    private AdminRepository adminRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    private AdminSigninRequestDTO createRequest(String adminId, String password) {
        return AdminSigninRequestDTO.builder()
                .adminId(adminId)
                .password(password)
                .build();
    }

    @Test
    @DisplayName("관리자는 올바른 아이디와 비밀번호로 로그인할 수 있다")
    void 로그인_성공() {
        // given
        Admin admin = Admin.builder()
                .adminId("admin1")
                .password("encodedPassword")
                .name("테스트관리자")
                .adminRole("ROLE_ADMIN")
                .build();

        when(adminRepository.findByAdminId("admin1")).thenReturn(Optional.of(admin));
        when(passwordEncoder.matches("rawPassword", "encodedPassword")).thenReturn(true);
        when(jwtTokenProvider.createAccessToken("admin1")).thenReturn("mock-access-token");

        AdminSigninRequestDTO request = createRequest("admin1", "rawPassword");

        // when
        AdminSigninResponseDTO response = adminAuthService.login(request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isEqualTo("mock-access-token");

    }

    @Test
    @DisplayName("존재하지 않는 관리자 아이디로 로그인하면 ADMIN_NOT_FOUND 예외가 발생한다")
    void 로그인_실패_존재하지않는관리자() {
        // given
        when(adminRepository.findByAdminId("unknown")).thenReturn(Optional.empty());

        AdminSigninRequestDTO request = createRequest("unknown", "anyPassword");

        // when
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> adminAuthService.login(request)
        );

        // then
        assertThat(exception.getMessage()).isEqualTo(ErrorCode.ADMIN_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("비밀번호가 일치하지 않으면 INVALID_PASSWORD 예외가 발생한다")
    void 로그인_실패_비밀번호불일치() {
        // given
        Admin admin = Admin.builder()
                .adminId("admin1")
                .password("encodedPassword")
                .name("테스트관리자")
                .adminRole("ROLE_ADMIN")
                .build();

        when(adminRepository.findByAdminId("admin1")).thenReturn(Optional.of(admin));
        when(passwordEncoder.matches("wrongPassword", "encodedPassword")).thenReturn(false);

        AdminSigninRequestDTO request = createRequest("admin1", "wrongPassword");

        // when
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> adminAuthService.login(request)
        );

        // then
        assertThat(exception.getMessage()).isEqualTo(ErrorCode.INVALID_PASSWORD.getMessage());
    }
}

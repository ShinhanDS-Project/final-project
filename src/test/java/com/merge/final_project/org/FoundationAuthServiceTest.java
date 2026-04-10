package com.merge.final_project.org;

import com.merge.final_project.admin.AdminRepository;
import com.merge.final_project.admin.adminlog.AdminLogService;
import com.merge.final_project.global.exceptions.BusinessException;
import com.merge.final_project.global.exceptions.ErrorCode;
import com.merge.final_project.global.jwt.JwtTokenProvider;
import com.merge.final_project.global.utils.FileUtil;
import com.merge.final_project.org.dto.FoundationSigninRequestDTO;
import com.merge.final_project.org.dto.FoundationSigninResponseDTO;
import com.merge.final_project.org.illegalfoundation.IllegalFoundationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FoundationAuthServiceTest {

    @InjectMocks
    private FoundationServiceImpl foundationService;

    @Mock private FoundationRepository foundationRepository;
    @Mock private IllegalFoundationRepository illegalFoundationRepository;
    @Mock private FileUtil upload;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private ApplicationEventPublisher eventPublisher;
    @Mock private AdminLogService adminLogService;
    @Mock private AdminRepository adminRepository;
    @Mock private JwtTokenProvider jwtTokenProvider;

    private Foundation activeFoundation;

    @BeforeEach
    void setUp() {
        activeFoundation = Foundation.builder()
                .foundationNo(1L)
                .foundationEmail("test@foundation.com")
                .foundationPassword("encodedPassword")
                .foundationName("테스트 단체")
                .accountStatus(AccountStatus.ACTIVE)
                .reviewStatus(ReviewStatus.APPROVED)
                .build();
    }

    private FoundationSigninRequestDTO request(String email, String password) {
        try {
            FoundationSigninRequestDTO dto = new FoundationSigninRequestDTO();
            var emailField = FoundationSigninRequestDTO.class.getDeclaredField("email");
            var passwordField = FoundationSigninRequestDTO.class.getDeclaredField("password");
            emailField.setAccessible(true);
            passwordField.setAccessible(true);
            emailField.set(dto, email);
            passwordField.set(dto, password);
            return dto;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @DisplayName("올바른 이메일과 비밀번호로 로그인하면 토큰이 반환된다")
    void 로그인_성공() {
        when(foundationRepository.findByFoundationEmail("test@foundation.com"))
                .thenReturn(Optional.of(activeFoundation));
        when(passwordEncoder.matches("rawPassword", "encodedPassword")).thenReturn(true);
        when(jwtTokenProvider.createGeneralAccessToken(
                eq("테스트 단체"), eq("test@foundation.com"), eq("ROLE_FOUNDATION"), eq(1L)))
                .thenReturn("mocked.jwt.token");

        FoundationSigninResponseDTO response = foundationService.login(request("test@foundation.com", "rawPassword"));

        assertThat(response.getAccessToken()).isEqualTo("mocked.jwt.token");
        assertThat(response.getTokenType()).isEqualTo("Bearer");
        assertThat(response.getFoundationNo()).isEqualTo(1L);
        assertThat(response.getFoundationName()).isEqualTo("테스트 단체");
        assertThat(response.getEmail()).isEqualTo("test@foundation.com");
    }

    @Test
    @DisplayName("존재하지 않는 이메일로 로그인하면 FOUNDATION_LOGIN_FAILED 예외가 발생한다")
    void 로그인_실패_이메일_없음() {
        when(foundationRepository.findByFoundationEmail("wrong@foundation.com"))
                .thenReturn(Optional.empty());

        BusinessException ex = assertThrows(BusinessException.class,
                () -> foundationService.login(request("wrong@foundation.com", "anyPassword")));

        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.FOUNDATION_LOGIN_FAILED);
    }

    @Test
    @DisplayName("비밀번호가 틀리면 FOUNDATION_LOGIN_FAILED 예외가 발생한다")
    void 로그인_실패_비밀번호_불일치() {
        when(foundationRepository.findByFoundationEmail("test@foundation.com"))
                .thenReturn(Optional.of(activeFoundation));
        when(passwordEncoder.matches("wrongPassword", "encodedPassword")).thenReturn(false);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> foundationService.login(request("test@foundation.com", "wrongPassword")));

        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.FOUNDATION_LOGIN_FAILED);
    }

    @Test
    @DisplayName("관리자 승인 대기 중인 단체는 로그인 시 FOUNDATION_NOT_ACTIVATED 예외가 발생한다")
    void 로그인_실패_미승인_단체() {
        // 불법단체 검토에서 CLEAN 판정 → 관리자 승인 대기 상태 (PRE_REGISTERED)
        Foundation pendingFoundation = Foundation.builder()
                .foundationNo(2L)
                .foundationEmail("pending@foundation.com")
                .foundationPassword("encodedPassword")
                .foundationName("대기 단체")
                .accountStatus(AccountStatus.PRE_REGISTERED)
                .reviewStatus(ReviewStatus.CLEAN)
                .build();

        when(foundationRepository.findByFoundationEmail("pending@foundation.com"))
                .thenReturn(Optional.of(pendingFoundation));
        when(passwordEncoder.matches("rawPassword", "encodedPassword")).thenReturn(true);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> foundationService.login(request("pending@foundation.com", "rawPassword")));

        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.FOUNDATION_NOT_ACTIVATED);
    }

    @Test
    @DisplayName("반려된 단체는 로그인 시 FOUNDATION_NOT_ACTIVATED 예외가 발생한다")
    void 로그인_실패_반려된_단체() {
        Foundation rejectedFoundation = Foundation.builder()
                .foundationNo(3L)
                .foundationEmail("rejected@foundation.com")
                .foundationPassword("encodedPassword")
                .foundationName("반려 단체")
                .accountStatus(AccountStatus.INACTIVE)
                .reviewStatus(ReviewStatus.REJECTED)
                .build();

        when(foundationRepository.findByFoundationEmail("rejected@foundation.com"))
                .thenReturn(Optional.of(rejectedFoundation));
        when(passwordEncoder.matches("rawPassword", "encodedPassword")).thenReturn(true);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> foundationService.login(request("rejected@foundation.com", "rawPassword")));

        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.FOUNDATION_NOT_ACTIVATED);
    }

    @Test
    @DisplayName("로그인 성공 시 ROLE_FOUNDATION 권한과 foundationNo가 토큰에 담긴다")
    void 로그인_성공_시_FOUNDATION_권한으로_토큰_발급() {
        when(foundationRepository.findByFoundationEmail("test@foundation.com"))
                .thenReturn(Optional.of(activeFoundation));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        when(jwtTokenProvider.createGeneralAccessToken(any(), any(), any(), any()))
                .thenReturn("token");

        foundationService.login(request("test@foundation.com", "rawPassword"));

        verify(jwtTokenProvider).createGeneralAccessToken(
                eq("테스트 단체"),
                eq("test@foundation.com"),
                eq("ROLE_FOUNDATION"),
                eq(1L)
        );
    }
}

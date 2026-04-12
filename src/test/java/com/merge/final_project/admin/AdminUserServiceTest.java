package com.merge.final_project.admin;

import com.merge.final_project.admin.adminlog.AdminLogService;
import com.merge.final_project.admin.service.AdminUserServiceImpl;
import com.merge.final_project.global.exceptions.BusinessException;
import com.merge.final_project.global.exceptions.ErrorCode;
import com.merge.final_project.user.users.User;
import com.merge.final_project.user.users.UserRepository;
import com.merge.final_project.user.users.UserStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminUserServiceTest {

    @InjectMocks
    private AdminUserServiceImpl adminUserService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AdminRepository adminRepository;

    @Mock
    private AdminLogService adminLogService;

    @BeforeEach
    void setUp() {
        var auth = new UsernamePasswordAuthenticationToken("testAdmin", null, List.of());
        SecurityContextHolder.getContext().setAuthentication(auth);

        Admin mockAdmin = mock(Admin.class);
        lenient().when(adminRepository.findByAdminId("testAdmin")).thenReturn(Optional.of(mockAdmin));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("비활성화된 회원을 활성화하면 ACTIVE 상태가 된다")
    void 회원_활성화_성공() {
        User user = mock(User.class);
        when(user.getStatus()).thenReturn(UserStatus.INACTIVE);
        when(user.getName()).thenReturn("홍길동");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        adminUserService.activateUser(1L);

        verify(user).activate();
    }

    @Test
    @DisplayName("이미 활성화된 회원을 활성화 시도하면 USER_ALREADY_ACTIVE 예외가 발생한다")
    void 회원_활성화_이미활성화_예외() {
        User user = mock(User.class);
        when(user.getStatus()).thenReturn(UserStatus.ACTIVE);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        BusinessException exception = assertThrows(BusinessException.class,
                () -> adminUserService.activateUser(1L));

        assertThat(exception.getMessage()).isEqualTo(ErrorCode.USER_ALREADY_ACTIVE.getMessage());
    }

    @Test
    @DisplayName("존재하지 않는 회원 활성화 시도 시 USER_NOT_FOUND 예외가 발생한다")
    void 회원_활성화_없는회원_예외() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        BusinessException exception = assertThrows(BusinessException.class,
                () -> adminUserService.activateUser(999L));

        assertThat(exception.getMessage()).isEqualTo(ErrorCode.USER_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("활성화된 회원을 비활성화하면 INACTIVE 상태가 된다")
    void 회원_비활성화_성공() {
        User user = mock(User.class);
        when(user.getStatus()).thenReturn(UserStatus.ACTIVE);
        when(user.getName()).thenReturn("홍길동");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        adminUserService.deactivateUser(1L);

        verify(user).deactivate();
    }

    @Test
    @DisplayName("이미 비활성화된 회원을 비활성화 시도하면 USER_ALREADY_INACTIVE 예외가 발생한다")
    void 회원_비활성화_이미비활성화_예외() {
        User user = mock(User.class);
        when(user.getStatus()).thenReturn(UserStatus.INACTIVE);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        BusinessException exception = assertThrows(BusinessException.class,
                () -> adminUserService.deactivateUser(1L));

        assertThat(exception.getMessage()).isEqualTo(ErrorCode.USER_ALREADY_INACTIVE.getMessage());
    }

    @Test
    @DisplayName("존재하지 않는 회원 비활성화 시도 시 USER_NOT_FOUND 예외가 발생한다")
    void 회원_비활성화_없는회원_예외() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        BusinessException exception = assertThrows(BusinessException.class,
                () -> adminUserService.deactivateUser(999L));

        assertThat(exception.getMessage()).isEqualTo(ErrorCode.USER_NOT_FOUND.getMessage());
    }
}

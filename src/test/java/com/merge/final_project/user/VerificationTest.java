package com.merge.final_project.user;

import com.merge.final_project.user.signUp.UserSignUpRepository;
import com.merge.final_project.user.users.LoginType;
import com.merge.final_project.user.verify.VerificationController;
import com.merge.final_project.user.verify.VerificationService;
import com.merge.final_project.user.verify.dto.UserVerifyRequestDTO;
import com.merge.final_project.user.verify.dto.UserVerifyResponseDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@SpringBootTest
class VerificationTest {

    @Autowired
    private VerificationController verificationController;

    @MockitoBean
    private UserSignUpRepository userSignUpRepository;

    @MockitoBean
    private VerificationService verificationService;

    @Test
    @DisplayName("성공: 유효한 이메일이면 인증번호 발송")
    void sendSuccessTest() {
        // given
        UserVerifyRequestDTO request = new UserVerifyRequestDTO("test@gmail.com", LoginType.GOOGLE);

        when(userSignUpRepository.existsByEmailAndLoginType("test@gmail.com", LoginType.GOOGLE))
                .thenReturn(false);

        // when
        ResponseEntity<UserVerifyResponseDTO> response = verificationController.send(request);

        // then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertTrue(response.getBody().isAvailable());
        assertEquals("인증번호가 발송되었습니다. 이메일을 확인해주세요", response.getBody().getMessage());

        verify(verificationService, times(1)).sendVerificationCode(any(UserVerifyRequestDTO.class));
    }

    @Test
    @DisplayName("실패: 이메일 형식이 올바르지 않으면 400 반환")
    void sendFailInvalidEmailTest() {
        // given
        UserVerifyRequestDTO request = new UserVerifyRequestDTO("invalid-email", LoginType.LOCAL);

        // when
        ResponseEntity<UserVerifyResponseDTO> response = verificationController.send(request);

        // then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertFalse(response.getBody().isAvailable());
        assertEquals("이메일 형식이 올바르지 않습니다.", response.getBody().getMessage());

        verify(userSignUpRepository, never()).existsByEmailAndLoginType(anyString(), eq(LoginType.LOCAL));
        verify(verificationService, never()).sendVerificationCode(any(UserVerifyRequestDTO.class));
    }

    @Test
    @DisplayName("실패: 이미 가입된 이메일이면 409 반환")
    void sendFailDuplicateEmailTest() {
        // given
        UserVerifyRequestDTO request = new UserVerifyRequestDTO("duplicate@gmail.com", LoginType.LOCAL);

        when(userSignUpRepository.existsByEmailAndLoginType("duplicate@gmail.com", LoginType.LOCAL))
                .thenReturn(true);

        // when
        ResponseEntity<UserVerifyResponseDTO> response = verificationController.send(request);

        // then
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertFalse(response.getBody().isAvailable());
        assertEquals("이미 가입된 이메일입니다.", response.getBody().getMessage());

        verify(verificationService, never()).sendVerificationCode(any(UserVerifyRequestDTO.class));
    }

    @Test
    @DisplayName("실패: 인증번호 재요청 제한이면 429 반환")
    void sendFailTooManyRequestsTest() {
        // given
        UserVerifyRequestDTO request = new UserVerifyRequestDTO("retry@gmail.com", LoginType.GOOGLE);

        when(userSignUpRepository.existsByEmailAndLoginType("retry@gmail.com", LoginType.GOOGLE))
                .thenReturn(false);

        doThrow(new IllegalStateException("인증번호는 1분마다 재요청할 수 있습니다."))
                .when(verificationService).sendVerificationCode(any(UserVerifyRequestDTO.class));

        // when
        ResponseEntity<UserVerifyResponseDTO> response = verificationController.send(request);

        // then
        assertEquals(HttpStatus.TOO_MANY_REQUESTS, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertTrue(response.getBody().isAvailable());
        assertEquals("인증번호는 1분마다 재요청할 수 있습니다.", response.getBody().getMessage());

        verify(verificationService, times(1)).sendVerificationCode(any(UserVerifyRequestDTO.class));
    }

    @Test
    @DisplayName("실패: 요청 횟수 초과이면 429 반환")
    void sendFailRequestCountExceededTest() {
        // given
        UserVerifyRequestDTO request = new UserVerifyRequestDTO("limit@gmail.com", LoginType.LOCAL);

        when(userSignUpRepository.existsByEmailAndLoginType("limit@gmail.com", LoginType.LOCAL))
                .thenReturn(false);

        doThrow(new IllegalStateException("인증번호 요청 횟수 (5회)를 초과했습니다. 나중에 다시 시도해주세요."))
                .when(verificationService).sendVerificationCode(any(UserVerifyRequestDTO.class));

        // when
        ResponseEntity<UserVerifyResponseDTO> response = verificationController.send(request);

        // then
        assertEquals(HttpStatus.TOO_MANY_REQUESTS, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertTrue(response.getBody().isAvailable());
        assertEquals("인증번호 요청 횟수 (5회)를 초과했습니다. 나중에 다시 시도해주세요.", response.getBody().getMessage());
    }

    @Test
    @DisplayName("성공: 같은 이메일이어도 loginType이 다르면 발송 가능")
    void sendSuccessSameEmailDifferentLoginTypeTest() {
        // given
        UserVerifyRequestDTO request = new UserVerifyRequestDTO("same@gmail.com", LoginType.GOOGLE);

        when(userSignUpRepository.existsByEmailAndLoginType("same@gmail.com", LoginType.GOOGLE))
                .thenReturn(false);

        // when
        ResponseEntity<UserVerifyResponseDTO> response = verificationController.send(request);

        // then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        verify(verificationService, times(1)).sendVerificationCode(any(UserVerifyRequestDTO.class));
    }

//    @Test
//    @DisplayName("실제 내 이메일로 인증 메일 발송 테스트")
//    void sendRealVerificationEmailTest() {
//
//
//        verificationService.sendVerificationCode(email);
//
//        System.out.println("인증 메일 발송 완료: " + email);
//    }
}
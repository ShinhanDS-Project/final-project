package com.merge.final_project.user;

import com.merge.final_project.user.users.LoginType;
import com.merge.final_project.user.verify.VerificationController;
import com.merge.final_project.user.verify.VerificationService;
import com.merge.final_project.user.verify.dto.UserVerifyCodeRequestDTO;
import com.merge.final_project.user.verify.dto.UserVerifyRequestDTO;
import com.merge.final_project.user.verify.dto.UserVerifyResponseDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest
class VerificationTest {

    @Autowired
    private VerificationController verificationController;

    @MockitoBean
    private VerificationService verificationService;

    @Test
    @DisplayName("인증번호 발송 요청을 서비스에 위임한다")
    void sendSuccessTest() {
        UserVerifyRequestDTO request = new UserVerifyRequestDTO("test@gmail.com", LoginType.GOOGLE);
        UserVerifyResponseDTO responseDto = UserVerifyResponseDTO.builder()
                .success(true)
                .available(true)
                .message("인증번호가 발송되었습니다.")
                .build();

        when(verificationService.sendVerificationCode(request)).thenReturn(responseDto);

        ResponseEntity<UserVerifyResponseDTO> response = verificationController.send(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(responseDto, response.getBody());
        verify(verificationService, times(1)).sendVerificationCode(request);
    }

    @Test
    @DisplayName("서비스 예외는 컨트롤러에서 그대로 전파된다")
    void sendFailDuplicateEmailTest() {
        UserVerifyRequestDTO request = new UserVerifyRequestDTO("duplicate@gmail.com", LoginType.LOCAL);

        when(verificationService.sendVerificationCode(request))
                .thenThrow(new IllegalStateException("이미 가입한 이메일입니다."));

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> verificationController.send(request)
        );

        assertEquals("이미 가입한 이메일입니다.", exception.getMessage());
        verify(verificationService, times(1)).sendVerificationCode(request);
    }

    @Test
    @DisplayName("인증코드가 맞으면 true를 반환한다")
    void confirmSuccessTest() {
        UserVerifyCodeRequestDTO request = new UserVerifyCodeRequestDTO("verify@gmail.com", "123456");

        when(verificationService.verifyCode("verify@gmail.com", "123456")).thenReturn(true);

        boolean result = verificationController.verify(request);

        assertTrue(result);
        verify(verificationService, times(1)).verifyCode("verify@gmail.com", "123456");
    }

    @Test
    @DisplayName("인증코드 확인 실패 예외는 그대로 전파된다")
    void confirmFailTest() {
        UserVerifyCodeRequestDTO request = new UserVerifyCodeRequestDTO("verify@gmail.com", "000000");

        when(verificationService.verifyCode("verify@gmail.com", "000000"))
                .thenThrow(new IllegalArgumentException("인증코드가 일치하지 않습니다."));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> verificationController.verify(request)
        );

        assertEquals("인증코드가 일치하지 않습니다.", exception.getMessage());
        verify(verificationService, times(1)).verifyCode("verify@gmail.com", "000000");
    }
}

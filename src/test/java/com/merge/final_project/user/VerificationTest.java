//package com.merge.final_project.user;
//
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.merge.final_project.global.jwt.JwtTokenProvider;
//import com.merge.final_project.user.signUp.UserSignUpRepository;
//import com.merge.final_project.user.users.LoginType;
//import com.merge.final_project.user.verify.VerificationController;
//import com.merge.final_project.user.verify.VerificationService;
//import com.merge.final_project.user.verify.dto.UserVerifyCodeRequestDTO;
//import com.merge.final_project.user.verify.dto.UserVerifyRequestDTO;
//import com.merge.final_project.user.verify.dto.UserVerifyResponseDTO;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
//import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.test.context.bean.override.mockito.MockitoBean;
//import org.springframework.http.MediaType;
//import org.springframework.test.web.servlet.MockMvc;
//
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.ArgumentMatchers.eq;
//import static org.mockito.BDDMockito.given;
//import static org.mockito.Mockito.never;
//import static org.mockito.Mockito.verify;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
//
//@WebMvcTest(VerificationController.class)
//@AutoConfigureMockMvc(addFilters = false)
//class VerificationTest {
//
//    @Autowired
//    private MockMvc mockMvc;
//    @MockitoBean
//    private JwtTokenProvider jwtTokenProvider;
//
//    private final ObjectMapper objectMapper = new ObjectMapper();
//
//    @MockitoBean
//    private VerificationService verificationService;
//
//    @Test
//    @DisplayName("성공: 회원가입 인증번호 발송")
//    void sendVerificationCodeSuccess() throws Exception {
//        UserVerifyRequestDTO request = new UserVerifyRequestDTO("test@gmail.com", LoginType.LOCAL);
//
//        UserVerifyResponseDTO response = UserVerifyResponseDTO.builder()
//                .success(true)
//                .message("인증번호가 발송되었습니다.")
//                .build();
//
//        given(verificationService.sendVerificationCode(any(UserVerifyRequestDTO.class)))
//                .willReturn(response);
//
//        mockMvc.perform(post("/api/auth/users/verification/send")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(request)))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.success").value(true))
//                .andExpect(jsonPath("$.message").value("인증번호가 발송되었습니다."));
//
//        verify(verificationService).sendVerificationCode(any(UserVerifyRequestDTO.class));
//    }
//
//    @Test
//    @DisplayName("실패: 이메일 형식이 잘못되면 400")
//    void sendVerificationCodeInvalidEmail() throws Exception {
//        UserVerifyRequestDTO request = new UserVerifyRequestDTO("invalid-email", LoginType.LOCAL);
//
//        mockMvc.perform(post("/api/auth/users/verification/send")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(request)))
//                .andExpect(status().isBadRequest());
//
//        verify(verificationService, never()).sendVerificationCode(any(UserVerifyRequestDTO.class));
//    }
//
//    @Test
//    @DisplayName("실패: loginType이 없으면 400")
//    void sendVerificationCodeLoginTypeNull() throws Exception {
//        String body = """
//                {
//                  "email": "test@gmail.com",
//                  "loginType": null
//                }
//                """;
//
//        mockMvc.perform(post("/api/auth/users/verification/send")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(body))
//                .andExpect(status().isBadRequest());
//
//        verify(verificationService, never()).sendVerificationCode(any(UserVerifyRequestDTO.class));
//    }
//
//    @Test
//    @DisplayName("성공: 인증번호 검증 성공")
//    void verifyCodeSuccess() throws Exception {
//        UserVerifyCodeRequestDTO request = UserVerifyCodeRequestDTO.builder()
//                .email("test@gmail.com")
//                .code("123456")
//                .build();
//
//        given(verificationService.verifyCode("test@gmail.com", "123456"))
//                .willReturn(true);
//
//        mockMvc.perform(post("/api/auth/users/verification/verify")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(request)))
//                .andExpect(status().isOk())
//                .andExpect(content().string("true"));
//
//        verify(verificationService).verifyCode("test@gmail.com", "123456");
//    }
//
//    @Test
//    @DisplayName("실패: 인증번호 검증 요청에서 이메일 형식이 잘못되면 400")
//    void verifyCodeInvalidEmail() throws Exception {
//        UserVerifyCodeRequestDTO request = UserVerifyCodeRequestDTO.builder()
//                .email("wrong-email")
//                .code("123456")
//                .build();
//
//        mockMvc.perform(post("/api/auth/users/verification/verify")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(request)))
//                .andExpect(status().isBadRequest());
//
//        verify(verificationService, never()).verifyCode(any(), any());
//    }
//
//    @Test
//    @DisplayName("실패: 인증번호가 비어 있으면 400")
//    void verifyCodeBlankCode() throws Exception {
//        UserVerifyCodeRequestDTO request = UserVerifyCodeRequestDTO.builder()
//                .email("test@gmail.com")
//                .code("")
//                .build();
//
//        mockMvc.perform(post("/api/auth/users/verification/verify")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(request)))
//                .andExpect(status().isBadRequest());
//
//        verify(verificationService, never()).verifyCode(any(), any());
//    }
//}
package com.merge.final_project.user;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.merge.final_project.global.jwt.JwtTokenProvider;
import com.merge.final_project.user.auth.UserAuthController;
import com.merge.final_project.user.users.UserService;
import com.merge.final_project.user.users.dto.login.UserLoginRequestDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;

import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

//@WebMvcTest(UserAuthController.class) //컨트롤러만 가볍게 테스트할때
@TestPropertySource(properties = "cookie.secure=false")
@Transactional
@AutoConfigureMockMvc // 자동으로 mockmvc 빈 생성
@SpringBootTest //--> db까지 엮어서 테스트
public class UserLoginLogoutTest {
    @Autowired
    private MockMvc mockMvc;

    private ObjectMapper objectMapper=new ObjectMapper();

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private UserService userService;

    @Test
    @DisplayName("로컬 로그인 성공 시 accessToken과 message를 반환한다")
    void userLoginSuccess() throws Exception {
        UserLoginRequestDTO request = new UserLoginRequestDTO();
        request.setEmail("test@gmail.com");
        request.setPassword("1234");

        given(userService.login(any(UserLoginRequestDTO.class)))
                .willReturn("mock-access-token");

        mockMvc.perform(post("/api/auth/login/user/local")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"test@gmail.com\",\"password\":\"1234\"}")
                .with(csrf())) // <--- 이 한 줄이 핵심!
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("mock-access-token"))
                .andExpect(jsonPath("$.message").value("로그인이 성공하였습니다"));
    }

    @Test
    @DisplayName("로컬 로그아웃 성공 시 메시지를 반환한다")
    void logoutLocalSuccess() throws Exception {
        mockMvc.perform(post("/api/auth/logout/user/local"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(" 로컬로그아웃 되었습니다."));
    }

    @Test
    @DisplayName("소셜 로그아웃 성공 시 Set-Cookie 헤더를 내려준다")
    void logoutSocialSuccess() throws Exception {
        mockMvc.perform(post("/api/auth/logout/user/social"))
                .andExpect(status().isOk())
                .andExpect(header().string("Set-Cookie",
                        org.hamcrest.Matchers.containsString("accessToken=")))
                .andExpect(jsonPath("$.message").value("소셜 로그아웃되었습니다."));
    }

    @Test
    @DisplayName("social-info 조회 성공 시 email과 name을 반환한다")
    void socialInfoSuccess() throws Exception {
        String token = "temp-token";

        given(jwtTokenProvider.validateToken(token)).willReturn(true);
        given(jwtTokenProvider.getTokenType(token)).willReturn("TEMP");
        given(jwtTokenProvider.getEmailFromToken(token)).willReturn("google@gmail.com");
        given(jwtTokenProvider.getNameFromToken(token)).willReturn("구글사용자");

        mockMvc.perform(get("/api/auth/social-info")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("google@gmail.com"))
                .andExpect(jsonPath("$.name").value("구글사용자"));
    }

    @Test
    @DisplayName("social-info 요청 시 Authorization 헤더가 없으면 400이 발생한다")
    void socialInfoNoHeader() throws Exception {
        mockMvc.perform(get("/api/auth/social-info"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("social-info 요청 시 TEMP 토큰이 아니면 401을 반환한다")
    void socialInfoNotTempToken() throws Exception {
        String token = "access-token";

        given(jwtTokenProvider.validateToken(token)).willReturn(true);
        given(jwtTokenProvider.getTokenType(token)).willReturn("ACCESS");
        given(jwtTokenProvider.getAdminRole(token)).willReturn("ROLE_USER");
        mockMvc.perform(get("/api/auth/social-info")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("temp토큰이 아닙니다."));
    }
}

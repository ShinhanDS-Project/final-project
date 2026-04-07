package com.merge.final_project.global.exceptions;

import com.merge.final_project.global.jwt.JwtTokenProvider;
import com.merge.final_project.user.signUp.UserSignUpRepository;
import com.merge.final_project.user.users.LoginType;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {
    private final JwtTokenProvider jwtTokenProvider;
    private final UserSignUpRepository userSignUpRepository;
    @Value("${app.frontend-url}")
    private String frontendUrl;

    //구글 회원가입| 로그인 관련 handler
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        OAuth2User oAuth2User = (OAuth2User)authentication.getPrincipal();
        String email= oAuth2User.getAttribute("email");
        String name= oAuth2User.getAttribute("name");


        // 1.회원가입 여부 확인
        if(!userSignUpRepository.existsByEmailAndLoginType(email, LoginType.GOOGLE)){
            //정보가 없는경우(--> 회원가입 기능)
            String tempToken=jwtTokenProvider.createTempToken(name,email);
            response.sendRedirect(frontendUrl + "/signup/google?token=" + tempToken);

        }
        else{
            //정보가 있는경우 로그인 (-- > 이건 로그인기능에서 구현)
            String accessToken=jwtTokenProvider.createSocialAccessToken(name,email); //소셜 로그인용 생성
            response.sendRedirect(frontendUrl + "/login/google?token=" + accessToken);

        }

    }
}

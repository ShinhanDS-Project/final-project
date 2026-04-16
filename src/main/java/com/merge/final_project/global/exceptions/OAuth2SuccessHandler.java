package com.merge.final_project.global.exceptions;

import com.merge.final_project.global.jwt.JwtTokenProvider;
import com.merge.final_project.user.signUp.UserSignUpRepository;
import com.merge.final_project.user.users.LoginType;
import com.merge.final_project.user.users.User;
import com.merge.final_project.user.users.UserRepository;
import com.merge.final_project.user.users.UserStatus;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {
    private final JwtTokenProvider jwtTokenProvider;
    private final UserSignUpRepository userSignUpRepository;
    private final UserRepository userRepository;
    @Value("${app.frontend-url}")
    private String frontendUrl;

    @Value("${cookie.secure}")
    private boolean secureCookie;

    //구글 회원가입| 로그인 관련 handler
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {

        OAuth2User oAuth2User = (OAuth2User)authentication.getPrincipal();
        String email= oAuth2User.getAttribute("email");
        String name= oAuth2User.getAttribute("name");


        // 1.회원가입 여부 확인
        Optional<User> optionalUser= userRepository.findByEmailAndLoginType(email,LoginType.GOOGLE);
        if(optionalUser.isEmpty()){
            //정보가 없는경우(--> 회원가입 기능)
            //토큰을 그대로 내려보내는 건 위험. 보안적으로 encode해서 내려보내기. 임시 토큰이고잠시 사용하는 용도이기 때문에 쿠키 처리 굳이?
            String tempToken = jwtTokenProvider.createTempToken(name, email);
            response.sendRedirect(frontendUrl + "/signup/google?token=" + URLEncoder.encode(tempToken, StandardCharsets.UTF_8));

        }
        else{
            //정보가 있는경우 로그인 (-- > 이건 로그인기능에서 구현)
            //상태여부 확인함->아닌경우에는 error로 내보내야함.
            if(!optionalUser.get().getStatus().equals(UserStatus.ACTIVE)){
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "비활성화된 계정은 로그인할 수 없습니다.");
                return;
            }
            //토큰을 그대로 노출 된 상태로 프론트로 전달하는 건 위험하므로 쿠키+ 보안 처리를 하고 제공한다. HttpOnly쿠키
            String accessToken=jwtTokenProvider.createSocialAccessToken(name,email,optionalUser.get().getUserNo()); //소셜 로그인용 생성

            ResponseCookie cookie = ResponseCookie.from("accessToken", accessToken)
                    .httpOnly(true) //js에서 쿠키를 읽을수없음. xss 노출 위험 내려감
                    .secure(secureCookie) // 로컬에서는 false 분기 고려-> 운영에서는 true 처리/ 로컬에서는 false 처리 해줘야함->application.properties에 수정
                    .path("/")
                    .sameSite("None") //백엔드와 프론트가 도메인이 다르면 쿠기가 크로스 사이트에서 오갈 수 있기 때문에 설정해줘야함.
                    // cross-site용 쿠키가 필요시엔 sameSite=none-secure 조합 사용
                    .build();

            response.addHeader("Set-Cookie", cookie.toString());
            response.sendRedirect(frontendUrl + "/login/google");

        }

    }
}

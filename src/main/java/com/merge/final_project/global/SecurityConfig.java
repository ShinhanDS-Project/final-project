package com.merge.final_project.global;

import com.merge.final_project.user.auth.oauth.CustomOAuth2UserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.SecurityFilterChain;

import java.net.URLEncoder;

@Configuration
public class SecurityConfig {
    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, CustomOAuth2UserService customOAuth2UserService) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // 테스트를 위해 CSRF 보호 비활성화

                .authorizeHttpRequests(auth -> auth
                        //회원가입 경로는 인증 없이 허용합니다.
                        .requestMatchers("/api/beneficiary/signup").permitAll()
                        .anyRequest().authenticated() // 그 외의 요청은 로그인이 필요함 -> 스프링 세큐리티
                )
                //2. Oauth 로그인 설정
                .oauth2Login(oauth2->{
                     oauth2.userInfoEndpoint(userInfo-> userInfo.userService(customOAuth2UserService))
                             .successHandler((request,response,authentication)->{
                                 OAuth2User oAuth2User = (OAuth2User)authentication.getPrincipal();
                                 String email= oAuth2User.getAttribute("email");
                                 String name= oAuth2User.getAttribute("name");

                                 //한글 이름 깨짐 방지
                                 String encodedName= URLEncoder.encode(name, "UTF-8");

                                 //추가 정보를 받을 프론트엔드 페이지로 이동(쿼리 파라미터로 전달)
                                 // 예: http://localhost:8080/extra-signup?email=test@gmail.com&name=홍길동
                                 response.sendRedirect("/extra-signup?email=" + email + "&name=" + encodedName);

                             })
        })
        return http.build();
    }

    //@Bean
}
package com.merge.final_project.global.config;


import com.merge.final_project.global.exceptions.OAuth2SuccessHandler;
import com.merge.final_project.global.jwt.*;
import com.merge.final_project.user.auth.oauth.CustomOAuth2UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {



    private final OAuth2SuccessHandler oAuth2SuccessHandler;
    private final CustomOAuth2UserService customOAuth2UserService;
    private final JwtTokenProvider jwtTokenProvider;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final JwtAccessDeniedHandler jwtAccessDeniedHandler;
    private final AdminJwtFilter adminJwtFilter;
    private final JwtFilter  jwtFilter;
    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

    //관리자 전용 필터.
    @Order(1)
    @Bean
    public SecurityFilterChain adminFilterChain(HttpSecurity http, AdminJwtFilter adminJwtFilter, JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint, JwtAccessDeniedHandler jwtAccessDeniedHandler) throws Exception {
        http
                .securityMatcher("/admin/**")
                .csrf(csrf -> csrf.disable()) // 테스트를 위해 CSRF 보호 비활성화
                .formLogin(form -> form.disable())  //jwt 발급할 것이어서 스프링 기본 로그인 페이지 비활성화
                .httpBasic(basic -> basic.disable())    //브라우저 팝업이나 로그인 방식 비활성화
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))   //jwt발급으로 로그인 하니까 세션 안 쓸 것.
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/admin/auth/login").permitAll()   //관리자 로그인
                        .anyRequest().hasAuthority("ROLE_ADMIN") // 어드민 권한이 있는 사용자만 해당 경로에 접근하게 하기 위함.
                )
                .exceptionHandling(ex -> ex     //에외처리 위함. 401/403
                        .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                        .accessDeniedHandler(jwtAccessDeniedHandler)
                )
                .addFilterBefore(adminJwtFilter, UsernamePasswordAuthenticationFilter.class);    //유저네임필터보다 관리자jwt 필터 먼저 실행할 것


        return http.build();
    }
         //은선이 코드랑 채원 코드 섞음
   @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // 1. 기본 보안 설정 비활성화 (Stateless API)
                .csrf(csrf -> csrf.disable())
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // 2. 경로별 권한 설정 (두 체인의 경로 통합)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/", 
                                "/api/auth/**", 
                                "/api/signup/**", 
                                "/api/beneficiary/signup", 
                                "/api/beneficiary/signin",
                                "/oauth2/**", 
                                "/login/**", 
                                "/social-info", 
                                "/error"
                        ).permitAll() // 인증 없이 접근 가능한 경로들
                        .requestMatchers("/finalReport/**").authenticated() // 인증이 필요한 경로
                        .anyRequest().authenticated() // 그 외 모든 요청은 인증 필수
                )

                // 3. OAuth2 로그인 설정
                .oauth2Login(oauth2 -> oauth2
                        .userInfoEndpoint(userInfo -> userInfo.userService(customOAuth2UserService))
                        .successHandler(oAuth2SuccessHandler)
                )

                // 4. JWT 필터 및 예외 처리
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                        .accessDeniedHandler(jwtAccessDeniedHandler)
                )
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)

                // 5. 로그아웃 설정(은선이꺼만 추가됨)
                .logout(logout -> logout
                        .logoutUrl("/api/beneficiary/logout")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll()
                );

        return http.build();
    }

}
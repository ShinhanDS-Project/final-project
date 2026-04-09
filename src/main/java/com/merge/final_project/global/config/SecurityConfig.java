package com.merge.final_project.global.config;

import com.merge.final_project.global.exceptions.OAuth2SuccessHandler;
import com.merge.final_project.global.jwt.*;
import com.merge.final_project.user.auth.oauth.CustomOAuth2UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
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
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final JwtAccessDeniedHandler jwtAccessDeniedHandler;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    /**
     * 1. 관리자 전용 필터 체인 (/admin/**)
     */
    @Order(1)
    @Bean
    public SecurityFilterChain adminFilterChain(HttpSecurity http, AdminJwtFilter adminJwtFilter) throws Exception {
        http
                .securityMatcher("/admin/**")
                .csrf(csrf -> csrf.disable())
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/admin/auth/login").permitAll()
                        .anyRequest().hasAuthority("ROLE_ADMIN")
                )
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                        .accessDeniedHandler(jwtAccessDeniedHandler)
                )
                .addFilterBefore(adminJwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * 2. 일반 사용자 및 수혜자용 필터 체인 (그 외 전체)
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, JwtFilter jwtFilter) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                .authorizeHttpRequests(auth -> auth
                        // 1. [공개 경로] 누구나 접근 가능
                        .requestMatchers(
                                "/",
                                "/error",
                                "/favicon.ico",
                                "/uploads/**",          // 💡 업로드된 사진 보기 허용
                                "/api/beneficiary/signup",
                                "/api/beneficiary/signin",
                                "/api/beneficiary/test/**", // 💡 수혜자 테스트 페이지
                                "/finalReport/test/**",     // 💡 보고서 테스트 페이지
                                "/api/auth/**",
                                "/api/signup/**",
                                "/api/blockchain/**",
                                "/oauth2/**",
                                "/login/**",
                                "/social-info",
                                "/users/support/**",
                                "/api/foundation/**"
                        ).permitAll()

                        // 2. [인증 경로] 로그인한 사용자만 가능
                        .requestMatchers("/finalReport/**").authenticated()

                        // 3. 그 외 모든 요청은 인증 필요
                        .anyRequest().authenticated()
                )

                // OAuth2 로그인 설정
                .oauth2Login(oauth2 -> oauth2
                        .userInfoEndpoint(userInfo -> userInfo.userService(customOAuth2UserService))
                        .successHandler(oAuth2SuccessHandler)
                )

                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                        .accessDeniedHandler(jwtAccessDeniedHandler)
                )

                // JWT 필터 추가
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)

                // 로그아웃 설정
                .logout(logout -> logout
                        .logoutUrl("/api/beneficiary/logout")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID", "accessToken") // 💡 토큰 쿠키도 함께 삭제
                        .permitAll()
                );

        return http.build();
    }
}

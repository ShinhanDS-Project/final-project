package com.merge.final_project.global.config;

import com.merge.final_project.global.jwt.JwtAccessDeniedHandler;
import com.merge.final_project.global.jwt.JwtAuthenticationEntryPoint;
import com.merge.final_project.global.jwt.JwtFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // [각하의 코드] 컨트롤러에서 직접 로그인을 처리하여 JWT를 발급하기 위해 AuthenticationManager를 노출합니다.
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(
            HttpSecurity http,
            JwtFilter jwtFilter,
            JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint,
            JwtAccessDeniedHandler jwtAccessDeniedHandler
    ) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // API 방식이므로 CSRF 보호 비활성화
                .formLogin(form -> form.disable()) // JWT를 사용하므로 기본 폼 로그인 비활성화
                .httpBasic(basic -> basic.disable()) // HTTP Basic 인증 비활성화

                // [develop 코드] JWT 인증 기반이므로 세션을 유지하지 않음 (Stateless)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                .authorizeHttpRequests(auth -> auth
                        // 1. 인증 없이 접근 가능한 경로 (회원가입, 로그인 등)
                        .requestMatchers("/api/beneficiary/signup", "/api/beneficiary/signin").permitAll()
                        .requestMatchers("/admin/auth/login").permitAll()
                        .requestMatchers("/error").permitAll()

                        // 2. [각하의 코드] 보고서 관련: 로그인한 사용자만 접근 허용
                        .requestMatchers("/finalReport/**").authenticated()

                        // 3. 그 외 모든 요청은 인증 필요
                        .anyRequest().authenticated()
                )

                // [develop 코드] 인증/인가 예외 처리 (401, 403)
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                        .accessDeniedHandler(jwtAccessDeniedHandler)
                )

                // [develop 코드] UsernamePasswordAuthenticationFilter 실행 전 JWT 필터를 먼저 수행
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)

                .logout(logout -> logout
                        .logoutUrl("/api/beneficiary/logout")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll()
                );

        return http.build();
    }
}
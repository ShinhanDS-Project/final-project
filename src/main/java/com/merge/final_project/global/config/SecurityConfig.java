package com.merge.final_project.global.config;

import com.merge.final_project.global.jwt.JwtAccessDeniedHandler;
import com.merge.final_project.global.jwt.JwtAuthenticationEntryPoint;
import com.merge.final_project.global.jwt.AdminJwtFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {
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
                        .anyRequest().hasAuthority("ROLE_ADMIN") // 그 외의 요청은 로그인이 필요함 -> 스프링 세큐리티
                )
                .exceptionHandling(ex -> ex     //에외처리 위함. 401/403
                        .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                        .accessDeniedHandler(jwtAccessDeniedHandler)
                )
                .addFilterBefore(adminJwtFilter, UsernamePasswordAuthenticationFilter.class);    //유저네임필터보다 관리자jwt 필터 먼저 실행할 것


        return http.build();
    }
}
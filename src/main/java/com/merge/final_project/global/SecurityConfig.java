package com.merge.final_project.global;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {
    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // 테스트를 위해 CSRF 보호 비활성화
                .authorizeHttpRequests(auth -> auth
                        //회원가입 경로는 인증 없이 허용합니다.
                        .requestMatchers("/api/beneficiary/signup").permitAll()
                        .anyRequest().authenticated() // 그 외의 요청은 로그인이 필요함 -> 스프링 세큐리티
                );

        return http.build();
    }

    //@Bean
}
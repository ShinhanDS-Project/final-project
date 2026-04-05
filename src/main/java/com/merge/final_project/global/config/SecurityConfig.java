package com.merge.final_project.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // 컨트롤러에서 '진짜' 로그인을 처리하기 위해 Manager를 외부로 노출합니다.
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // API 방식 테스트를 위해 비활성화
                .authorizeHttpRequests(auth -> auth
                        // 1. 수혜자 가입 및 직접 만든 로그인 API는 모두 허용
                        .requestMatchers("/api/beneficiary/signup", "/api/beneficiary/signin").permitAll()
                        .requestMatchers("/error").permitAll()

                        // 2. 보고서 관련: 테스트 중에는 permitAll, 완성 후에는 .hasRole("BENEFICIARY") 권장
                        .requestMatchers("/finalReport/**").permitAll()

                        // 3. 그 외 나머지는 인증 필요
                        .anyRequest().authenticated()
                )
                // 직접 만든 API 로그인을 쓰므로 formLogin()은 기본 설정만 남기거나 끕니다.
                // 페이지 이동(Redirect)이 발생하는 설정을 지워야 404 에러를 방지할 수 있습니다.
                .formLogin(form -> form.disable())

                .logout(logout -> logout
                        .logoutUrl("/api/beneficiary/logout")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll()
                );

        return http.build();
    }
}
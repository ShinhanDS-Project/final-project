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
     * 2. 수혜자 및 리액트 전용 API 필터 체인
     */
    @Order(2)
    @Bean
    public SecurityFilterChain beneficiaryFilterChain(HttpSecurity http, JwtFilter jwtFilter) throws Exception {
        http
                .securityMatcher("/api/beneficiary/**", "/finalReport/**", "/api/v1/**")
                .csrf(csrf -> csrf.disable())
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // 수혜자 로그인/회원가입은 공개 (v1 포함)
                        .requestMatchers("/api/beneficiary/signup", "/api/beneficiary/signin").permitAll()
                        .requestMatchers("/api/v1/beneficiary/signup", "/api/v1/beneficiary/signin").permitAll()
                        // 그 외 수혜자/보고서 관련은 인증 필요
                        .anyRequest().authenticated()
                )
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                        .accessDeniedHandler(jwtAccessDeniedHandler)
                )
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .logout(logout -> logout
                        .logoutUrl("/api/beneficiary/logout")
                        .logoutSuccessUrl("/api/beneficiary/signin")
                        .deleteCookies("JSESSIONID", "accessToken")
                        .permitAll()
                );

        return http.build();
    }

    /**
     * 3. 기부단체 전용 필터 체인 (/api/foundation/**)
     */
    @Order(3)
    @Bean
    public SecurityFilterChain foundationFilterChain(HttpSecurity http, JwtFilter jwtFilter) throws Exception {
        http
                .securityMatcher("/api/foundation/**")
                .csrf(csrf -> csrf.disable())
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/api/foundation/signup",
                                "/api/foundation/check-brn",
                                "/api/foundation/all",
                                "/api/foundation/login",
                                "/api/foundation/logout",
                                "/api/foundation/campaigns/**"
                        ).permitAll()
                        .anyRequest().hasAuthority("ROLE_FOUNDATION")
                )
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                        .accessDeniedHandler(jwtAccessDeniedHandler)
                )
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * 4. 일반 사용자 및 공통 필터 체인 (Default)
     */
    @Bean
    public SecurityFilterChain defaultFilterChain(HttpSecurity http, JwtFilter jwtFilter) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/", "/error", "/favicon.ico", "/uploads/**",
                                "/api/auth/**", "/api/signup/**", "/api/blockchain/**",
                                "/oauth2/**", "/login/**", "/social-info", "/users/support/**"
                        ).permitAll()
                        .anyRequest().authenticated()
                )
                .oauth2Login(oauth2 -> oauth2
                        .userInfoEndpoint(userInfo -> userInfo.userService(customOAuth2UserService))
                        .successHandler(oAuth2SuccessHandler)
                )
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                        .accessDeniedHandler(jwtAccessDeniedHandler)
                )
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}

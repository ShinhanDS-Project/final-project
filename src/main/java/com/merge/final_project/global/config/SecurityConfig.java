package com.merge.final_project.global.config;

import com.merge.final_project.global.exceptions.OAuth2SuccessHandler;
import com.merge.final_project.global.jwt.*;
import com.merge.final_project.user.auth.oauth.CustomOAuth2UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
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
import org.springframework.http.HttpMethod;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final OAuth2SuccessHandler oAuth2SuccessHandler;
    private final CustomOAuth2UserService customOAuth2UserService;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final JwtAccessDeniedHandler jwtAccessDeniedHandler;

    @Value("${CORS_ALLOWED_ORIGIN}")
    private String allowedOrigin;

    @Value("${app.frontend-url}")
    String frontendUrl;
    //CORS 설정을 위해 추가합니다
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of(
                "http://3.34.125.62:8090", // 실제 프론트엔드 서버 주소
                "http://localhost:5173",   // 로컬 테스트용
                "http://localhost:3000",   // 로컬 테스트용
                "http://merge.io.kr:8090",
                "http://192.168.0.220:8090",
                allowedOrigin              // .env나 properties에서 가져온 값
        ));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    /**
     * 비밀번호 인코더 빈.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * 스프링 시큐리티 AuthenticationManager 주입용 빈.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    //관리자
    @Order(1)
    @Bean
    public SecurityFilterChain adminFilterChain(HttpSecurity http, AdminJwtFilter adminJwtFilter) throws Exception {
        http
                .securityMatcher("/admin/**")
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/admin/auth/login", "/admin/auth/logout").permitAll()
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
     * 2. 수혜자 및 리액트 API 전용 필터 체인
     */
    @Order(2)
    @Bean
    public SecurityFilterChain beneficiaryFilterChain(HttpSecurity http, JwtFilter jwtFilter) throws Exception {
        http
                .securityMatcher("/api/beneficiary/**", "/finalReport/**", "/api/v1/**", "/api/redemptions/**")
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable()) // 💡 팝업 방지 확실히!
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // 수혜자 및 공용 API 개방
                        .requestMatchers("/api/beneficiary/signup", "/api/beneficiary/signin").permitAll()
                        .requestMatchers("/api/v1/beneficiary/signup", "/api/v1/beneficiary/signin").permitAll()
                        .requestMatchers("/api/v1/final-reports/campaigns").permitAll() // 필요 시 개방
                        .requestMatchers("/api/v1/final-reports/campaign/**").permitAll() // 보고서 상세 조회 (공개)

                        // 그 외 모든 요청은 인증 필요
                        .anyRequest().authenticated()
                )
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(jwtAuthenticationEntryPoint) // 💡 팝업 대신 JSON 응답
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

    // 기부단체
    @Order(3)
    @Bean
    public SecurityFilterChain foundationFilterChain(HttpSecurity http, JwtFilter jwtFilter) throws Exception {
        http
                .securityMatcher("/api/foundation/**")
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/api/foundation/signup",    // 가입 신청
                                "/api/foundation/check-brn", // 사업자등록번호 중복 체크
                                "/api/foundation/all",       // 단체 목록 조회 (공개)
                                "/api/foundation/login",     // 로그인
                                "/api/foundation/logout",    // 로그아웃 (토큰 만료 후에도 호출 가능해야 함)
                                "/api/foundation/campaigns"  // 캠페인 목록 조회 (공개)

                        ).permitAll()
                        // GET 한정 공개 — 일반 사용자 기부단체 상세 조회 (숫자 ID 경로만 허용, /me 등 제외)
                        .requestMatchers(HttpMethod.GET, "/api/foundation/{foundationNo:\\d+}").permitAll()
                        // GET 한정 공개 — POST /register는 ROLE_FOUNDATION 필요
                        .requestMatchers(HttpMethod.GET, "/api/foundation/campaigns/*").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/foundation/campaigns/*/detail").permitAll()
                        // GET 한정 공개 — 기부단체 지갑 정보 및 캠페인 목록 (foundationNo 경로 파라미터)
                        .requestMatchers(HttpMethod.GET, "/api/foundation/*/wallet").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/foundation/*/campaigns").permitAll()
                        // 호연
                        .requestMatchers(HttpMethod.GET, "/api/foundation/campaigns/register").permitAll()
                        // 그 외 단체 전용 기능은 ROLE_FOUNDATION 필요
                        .anyRequest().hasAuthority("ROLE_FOUNDATION")
                )
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                        .accessDeniedHandler(jwtAccessDeniedHandler)
                )
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }


    //일반 사용자
    @Order(4)
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, JwtFilter jwtFilter) throws Exception {

        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))  // [가빈] CORS 설정을 위해 추가
                .csrf(csrf -> csrf.disable())
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
                .authorizeHttpRequests(auth -> auth
                        // 1. [공개 경로] 누구나 접근 가능
                        .requestMatchers(
                                "/",
                                "/error",
                                "/favicon.ico",
                                "/uploads/**",
                                "/api/auth/**",
                                "/api/signup/**", // /api/signup/nickname 포함
                                // 블록체인 대시보드/조회 API는 프론트 대시보드 초기 연동을 위해 우선 공개.
                                // 추후 인증 정책 확정 시 role 기반 접근 제어로 전환 가능.
                                "/api/blockchain/**",
                                "/oauth2/**",
                                "/login/**",
                                "/social-info",
                                "/api/donation/public/stats",    // main의 기부 누적 조회
                                "/api/donation/public/home-hub",
                                "/api/donation/public/recent-donations",
                                "/api/donation/public/latest-campaigns"
                        ).permitAll()
                        // UserController의 API 중 로그인이 필요 없는 기능만 명시적으로 허용
                        .requestMatchers(HttpMethod.POST,
                                "/users/support/email",
                                "/users/support/password/reset/request",
                                "/users/support/password/reset/verify",
                                "/users/support/password/reset/confirm"
                        ).permitAll()
                        .requestMatchers(HttpMethod.GET, "/users/support/see").permitAll()

                        // 2. [인증 경로] 로그인한 사용자만 가능
                        .requestMatchers("/finalReport/**").authenticated()

                        // 3. 그 외 모든 요청은 인증 필요 (마이페이지 관련 API들이 여기에 해당됨)
                        .anyRequest().authenticated()
                )

                // OAuth2 로그인 설정
                .oauth2Login(oauth2 -> oauth2
                        .userInfoEndpoint(userInfo -> userInfo.userService(customOAuth2UserService))
                        .successHandler(oAuth2SuccessHandler)
                        .failureHandler((request, response, exception) -> {
                            exception.printStackTrace();
                            response.sendRedirect(frontendUrl+"/login?error=" +
                                    URLEncoder.encode(exception.getMessage(), StandardCharsets.UTF_8));
                        })
                )

                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                        .accessDeniedHandler(jwtAccessDeniedHandler)
                )

                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

}

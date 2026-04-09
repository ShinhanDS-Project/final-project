package com.merge.final_project.global.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String token = resolveToken(request);

        // 1. 토큰이 존재하고 유효한지 체크
        if (token != null && jwtTokenProvider.validateToken(token)) {

            // 2. 토큰 타입 추출 (ACCESS vs TEMP)
            String type = jwtTokenProvider.getTokenType(token);

            // 💡 중요: ACCESS 타입이거나 타입 정보가 없는 경우(관리자 토큰)에만 인증 처리
            if (type == null || "ACCESS".equals(type)) {

                // 3. 토큰에서 정보 추출
                String email = jwtTokenProvider.getAdminId(token); // 기본적으로 subject(이메일)를 가져옴
                String role = jwtTokenProvider.getAdminRole(token);
                Long pk = jwtTokenProvider.getReceiverNo(token); // 수혜자/사용자 번호(PK)

                if (email != null && role != null) {
                    // 4. 시큐리티 권한 객체 생성 (Principal에 User 객체 사용)
                    User principal = new User(email, "", List.of(new SimpleGrantedAuthority(role)));

                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());

                    // 5. 추가 정보(PK) 저장
                    authentication.setDetails(pk);

                    // 6. 시큐리티 컨텍스트에 저장
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
        }

        filterChain.doFilter(request, response);
    }

    private String resolveToken(HttpServletRequest request) {
        // 1. HTTP 헤더 확인 (Bearer 방식)
        String bearer = request.getHeader("Authorization");
        if (bearer != null && bearer.startsWith("Bearer ")) {
            return bearer.substring(7);
        }

        // 2. 쿠키 확인 (브라우저 주소창 직접 이동 시 인증 유지용)
        if (request.getCookies() != null) {
            for (jakarta.servlet.http.Cookie cookie : request.getCookies()) {
                if ("accessToken".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }
}

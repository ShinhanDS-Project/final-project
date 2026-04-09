package com.merge.final_project.global.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;

    // JwtFilter.java의 doFilterInternal 메서드 수정 내용
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String token = resolveToken(request);

        // 1. 토큰이 존재하고 유효한지 먼저 체크
        if (token != null && jwtTokenProvider.validateToken(token)) {

            // 2. 토큰 타입 추출 (ACCESS vs TEMP)
            String type = jwtTokenProvider.getTokenType(token);

            // 중요: ACCESS 타입일 때만 SecurityContext에 인증 정보 저장
            // TEMP(소셜 가입 대기) 토큰은 인증이 필요한 API에 접근하면 안 됨
            if ("ACCESS".equals(type)) {
                String email = jwtTokenProvider.getEmailFromToken(token);
                String role = jwtTokenProvider.getAdminRole(token); // Claims에서 "role" 꺼내기-> 메서드 이름 변경해야함
                Long receiverNo = jwtTokenProvider.getReceiverNo(token);

                // 3. 권한 객체 생성 (사용자가 USER든 BENEFICIARY든 토큰 내 role에 따라 생성)
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                email, null, List.of(new SimpleGrantedAuthority(role))
                        );
                authentication.setDetails(receiverNo);

                // 4. 시큐리티 컨텍스트에 저장
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }

        // 다음 필터로 진행
        filterChain.doFilter(request, response);
    }

    private String resolveToken(HttpServletRequest request) {
        String bearer = request.getHeader("Authorization");
        if (bearer != null && bearer.startsWith("Bearer ")) {
            return bearer.substring(7);
        }
        return null;
    }
}
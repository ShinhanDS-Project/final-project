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
public class AdminJwtFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String token = resolveToken(request);

        // 토큰이 null이 아니고 유효한 토큰이면 관리자 식별 정보 추출
        if (token != null && jwtTokenProvider.validateToken(token)) {

            String adminId = jwtTokenProvider.getAdminId(token);
            String role = jwtTokenProvider.getAdminRole(token);

            if (!"ROLE_ADMIN".equals(role)) { // 이미 꺼낸 값 재사용 → 추가 비용 없음
                filterChain.doFilter(request, response);
                return;
            }
            //관리자 식별 정보 추출해서 해당 정보로 관리자 권한 객체 생성
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            adminId, null, List.of(new SimpleGrantedAuthority(role))
                    );

            //시큐리티컨텍스트에 저장 => 인가할 수 있도록 스프링 시큐리티에게 전달
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }
        //다음 필터로 넘김
        filterChain.doFilter(request, response);
    }

    //토큰 헤더에서 Bearer 찾아서 실제 jwt 토큰만 꺼냄.
    private String resolveToken(HttpServletRequest request) {

        String bearer = request.getHeader("Authorization");

        if (bearer != null && bearer.startsWith("Bearer ")) {
            return bearer.substring(7);
        }

        return null;
    }
}

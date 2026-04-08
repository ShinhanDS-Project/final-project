package com.merge.final_project.recipient.beneficiary.controller;

import com.merge.final_project.global.jwt.JwtTokenProvider;
import com.merge.final_project.recipient.beneficiary.dto.BeneficiarySigninRequestDTO;
import com.merge.final_project.recipient.beneficiary.dto.BeneficiarySignupRequestDTO;
import com.merge.final_project.recipient.beneficiary.service.BeneficiaryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController // 다시 RestController로 복구
@RequestMapping("/api/beneficiary")
@RequiredArgsConstructor
@Log4j2
public class BeneficiaryController {

    private final BeneficiaryService beneficiaryService;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;

    @PostMapping("/signin")
    public ResponseEntity<?> login(@RequestBody BeneficiarySigninRequestDTO loginDto, 
                                 jakarta.servlet.http.HttpServletResponse response) {
        // 1. 인증 시도
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginDto.getEmail(), loginDto.getPassword())
        );

        // 2. JWT 토큰 생성
        String accessToken = jwtTokenProvider.createAdminAccessToken(
                authentication.getName(),
                authentication.getAuthorities().iterator().next().getAuthority()
        );

        // 3. 💡 서버에서 직접 쿠키 발급 (브라우저 주소창 이동 시 인증 유지용)
        jakarta.servlet.http.Cookie cookie = new jakarta.servlet.http.Cookie("accessToken", accessToken);
        cookie.setPath("/");
        cookie.setHttpOnly(true); // JS에서 접근 불가 (보안)
        cookie.setMaxAge(60 * 60 * 24); // 1일 유지
        response.addCookie(cookie);

        log.info("수혜자 로그인 성공 및 쿠키 발급 완료: {}", loginDto.getEmail());

        // 토큰 문자열 반환 (필요 시 클라이언트에서 사용 가능)
        return ResponseEntity.ok(accessToken);
    }

    @PostMapping("/signup")
    public ResponseEntity<Long> signup(@RequestBody BeneficiarySignupRequestDTO dto) {
        log.info("수혜자 회원가입 시도: {}", dto.getEmail());
        Long beneficiaryNo = beneficiaryService.signup(dto);
        return ResponseEntity.ok(beneficiaryNo);
    }
}

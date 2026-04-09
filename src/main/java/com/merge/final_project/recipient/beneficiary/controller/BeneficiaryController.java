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
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller // HTML 뷰 반환을 위해 Controller 사용
@RequestMapping("/api/beneficiary")
@RequiredArgsConstructor
@Log4j2
public class BeneficiaryController {

    private final BeneficiaryService beneficiaryService;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * 로그인 테스트 페이지 이동
     */
    @GetMapping("/test/login")
    public String testLoginPage() {
        return "beneficiary/test-login";
    }

    /**
     * 회원가입 테스트 페이지 이동
     */
    @GetMapping("/test/signup")
    public String signupPage() {
        return "beneficiary/test-signup";
    }

    /**
     * 로그인 처리 (API)
     */
    @PostMapping("/signin")
    @ResponseBody // JSON 응답
    public ResponseEntity<?> login(@RequestBody BeneficiarySigninRequestDTO loginDto, 
                                 jakarta.servlet.http.HttpServletResponse response) {
        
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginDto.getEmail(), loginDto.getPassword())
        );

        String accessToken = jwtTokenProvider.createAdminAccessToken(
                authentication.getName(),
                authentication.getAuthorities().iterator().next().getAuthority()
        );

        jakarta.servlet.http.Cookie cookie = new jakarta.servlet.http.Cookie("accessToken", accessToken);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setMaxAge(60 * 60 * 24);
        response.addCookie(cookie);

        log.info("수혜자 로그인 성공 및 쿠키 발급 완료: {}", loginDto.getEmail());

        return ResponseEntity.ok(accessToken);
    }

    /**
     * 회원가입 처리 (API)
     */
    @PostMapping("/signup")
    @ResponseBody // JSON 응답
    public ResponseEntity<Long> signup(@RequestBody BeneficiarySignupRequestDTO dto) {
        log.info("수혜자 회원가입 시도: {}", dto.getEmail());
        Long beneficiaryNo = beneficiaryService.signup(dto);
        return ResponseEntity.ok(beneficiaryNo);
    }
}

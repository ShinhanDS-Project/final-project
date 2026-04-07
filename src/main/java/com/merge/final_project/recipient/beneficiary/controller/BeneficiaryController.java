package com.merge.final_project.recipient.beneficiary.controller;

import com.merge.final_project.global.jwt.JwtTokenProvider;
import com.merge.final_project.recipient.beneficiary.dto.BeneficiarySigninRequestDTO;
import com.merge.final_project.recipient.beneficiary.dto.BeneficiarySignupRequestDTO;
import com.merge.final_project.recipient.beneficiary.service.BeneficiaryService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/beneficiary")
@RequiredArgsConstructor
@Log4j2
public class BeneficiaryController {

    private final BeneficiaryService beneficiaryService;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider; // 1. 주입 추가

    @PostMapping("/signin")
    public ResponseEntity<?> login(@RequestBody BeneficiarySigninRequestDTO loginDto) {
        // 2. 인증 시도
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginDto.getEmail(), loginDto.getPassword())
        );

        // 3. 인증 정보를 기반으로 JWT 토큰 생성
        // (주의: 현재 JwtTokenProvider에 관리자용 메서드만 있다면 수혜자용으로 이름을 바꾸거나 공용화가 필요할 수 있습니다.)
        String accessToken = jwtTokenProvider.createAdminAccessToken(
                authentication.getName(),
                authentication.getAuthorities().iterator().next().getAuthority()
        );

        log.info("수혜자 로그인 성공 및 JWT 발급 완료: {}", loginDto.getEmail());

        // 4. 세션 대신 토큰을 반환
        return ResponseEntity.ok(accessToken);
    }
    @PostMapping("/signup")
    public ResponseEntity<Long> signup(@RequestBody BeneficiarySignupRequestDTO dto) {
        log.info("수혜자 회원가입 시도: {}", dto.getEmail());

        // BeneficiaryService의 signup 메서드를 호출하여 회원가입 처리
        Long beneficiaryNo = beneficiaryService.signup(dto);

        log.info("수혜자 회원가입 성공. 번호: {}", beneficiaryNo);
        return ResponseEntity.ok(beneficiaryNo);
    }
}

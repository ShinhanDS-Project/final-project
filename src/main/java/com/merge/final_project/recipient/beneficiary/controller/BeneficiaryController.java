package com.merge.final_project.recipient.beneficiary.controller;

import com.merge.final_project.recipient.beneficiary.dto.BeneficiarySigninRequestDTO;
import com.merge.final_project.recipient.beneficiary.dto.BeneficiarySignupRequestDTO;
import com.merge.final_project.recipient.beneficiary.service.BeneficiaryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
    @PostMapping("/signup")
    public ResponseEntity<Long> signup(@RequestBody BeneficiarySignupRequestDTO dto){
        Long beneficiaryNo = beneficiaryService.signup(dto);
        return ResponseEntity.ok(beneficiaryNo);
    }
    @PostMapping("/signin")
    public ResponseEntity<?> login(@RequestBody BeneficiarySigninRequestDTO loginDto) {
        // 1. 토큰 생성
        UsernamePasswordAuthenticationToken token =
                new UsernamePasswordAuthenticationToken(loginDto.getEmail(), loginDto.getPassword());

        // 2. 실제 인증 (여기서 UserDetailsService의 loadUserByUsername이 돌아감)
        Authentication authentication = authenticationManager.authenticate(token);

        // 3. 인증 정보를 세션에 저장
        SecurityContextHolder.getContext().setAuthentication(authentication);

        return ResponseEntity.ok("수혜자  로그인 성공 (JSESSIONID 발급됨)");
    }

}

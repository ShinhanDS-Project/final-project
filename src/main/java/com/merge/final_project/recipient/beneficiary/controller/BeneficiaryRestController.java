package com.merge.final_project.recipient.beneficiary.controller;

import com.merge.final_project.global.jwt.JwtTokenProvider;
import com.merge.final_project.recipient.beneficiary.dto.BeneficiaryInfoResponseDTO;
import com.merge.final_project.recipient.beneficiary.dto.BeneficiarySigninRequestDTO;
import com.merge.final_project.recipient.beneficiary.dto.BeneficiarySignupRequestDTO;
import com.merge.final_project.recipient.beneficiary.dto.BeneficiaryUpdateRequestDTO;
import com.merge.final_project.recipient.beneficiary.service.BeneficiaryService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/v1/beneficiary")
@RequiredArgsConstructor
@Log4j2
public class
BeneficiaryRestController {

    private final BeneficiaryService beneficiaryService;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * 1. 회원가입 API
     */
    @PostMapping("/signup")
    public ResponseEntity<Long> signup(@RequestBody BeneficiarySignupRequestDTO dto) {
        log.info("리액트 수혜자 회원가입 시도: {}", dto.getEmail());
        return ResponseEntity.ok(beneficiaryService.signup(dto));
    }

    /**
     * 2. 로그인 API (쿠키 발급)
     */
    @PostMapping("/signin")
    public ResponseEntity<String> login(@RequestBody BeneficiarySigninRequestDTO loginDto, HttpServletResponse response) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginDto.getEmail(), loginDto.getPassword())
        );

        String accessToken = jwtTokenProvider.createAdminAccessToken(
                authentication.getName(),
                authentication.getAuthorities().iterator().next().getAuthority()
        );

        Cookie cookie = new Cookie("accessToken", accessToken);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setMaxAge(60 * 60 * 24);
        response.addCookie(cookie);

        return ResponseEntity.ok("success");
    }

    /**
     * 3. 내 상세 정보 조회 API
     */
    @GetMapping("/me")
    public ResponseEntity<BeneficiaryInfoResponseDTO> getMyInfo(Principal principal) {
        return ResponseEntity.ok(beneficiaryService.getMyDetailInfo(principal.getName()));
    }

    /**
     * 4. 내 정보 수정 API
     */
    @PutMapping("/me")
    public ResponseEntity<String> updateMyInfo(@RequestBody BeneficiaryUpdateRequestDTO dto, Principal principal) {
        beneficiaryService.updateMyInfo(principal.getName(), dto);
        return ResponseEntity.ok("success");
    }
}

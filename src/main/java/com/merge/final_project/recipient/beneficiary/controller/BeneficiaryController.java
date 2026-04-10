package com.merge.final_project.recipient.beneficiary.controller;

import com.merge.final_project.global.jwt.JwtTokenProvider;
import com.merge.final_project.recipient.beneficiary.dto.BeneficiarySigninRequestDTO;
import com.merge.final_project.recipient.beneficiary.dto.BeneficiarySignupRequestDTO;
import com.merge.final_project.recipient.beneficiary.dto.BeneficiaryUpdateRequestDTO;
import com.merge.final_project.recipient.beneficiary.service.BeneficiaryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/api/beneficiary")
@RequiredArgsConstructor
@Log4j2
public class BeneficiaryController {

    private final BeneficiaryService beneficiaryService;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * 로그인 페이지 이동
     */
    @GetMapping("/signin")
    public String signinPage() {
        return "beneficiary/signin";
    }

    /**
     * 회원가입 페이지 이동
     */
    @GetMapping("/signup")
    public String signupPage() {
        return "beneficiary/signup";
    }

    /**
     * 회원 정보 수정 페이지 이동
     */
    @GetMapping("/edit")
    public String editPage(java.security.Principal principal, Model model) {
        if (principal == null) return "redirect:/api/beneficiary/signin";

        BeneficiaryUpdateRequestDTO myInfo = beneficiaryService.getMyInfo(principal.getName());
        model.addAttribute("myInfo", myInfo);
        return "beneficiary/edit";
    }

    /**
     * 회원 정보 수정 처리 (API)
     */
    @PostMapping("/edit")
    @ResponseBody
    public ResponseEntity<String> updateInfo(@RequestBody BeneficiaryUpdateRequestDTO dto, java.security.Principal principal) {
        if (principal == null) return ResponseEntity.status(401).body("로그인 필요");

        beneficiaryService.updateMyInfo(principal.getName(), dto);
        return ResponseEntity.ok("정보 수정 완료");
    }

    /**
     * 로그인 처리 (API)
     */
    @PostMapping("/signin")
    @ResponseBody
    public ResponseEntity<?> login(@RequestBody BeneficiarySigninRequestDTO loginDto, 
                                 jakarta.servlet.http.HttpServletResponse response) {
        
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginDto.getEmail(), loginDto.getPassword())
        );

        // createAdminAccessToken -> 추후 수혜자 전용 메서드명으로 변경 권장
        String accessToken = jwtTokenProvider.createAdminAccessToken(
                authentication.getName(),
                authentication.getAuthorities().iterator().next().getAuthority()
        );

        jakarta.servlet.http.Cookie cookie = new jakarta.servlet.http.Cookie("accessToken", accessToken);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setMaxAge(60 * 60 * 24);
        response.addCookie(cookie);

        log.info("수혜자 로그인 성공: {}", loginDto.getEmail());

        return ResponseEntity.ok(accessToken);
    }

    /**
     * 회원가입 처리 (API)
     */
    @PostMapping("/signup")
    @ResponseBody
    public ResponseEntity<Long> signup(@RequestBody BeneficiarySignupRequestDTO dto) {
        log.info("수혜자 회원가입 시도: {}", dto.getEmail());
        Long beneficiaryNo = beneficiaryService.signup(dto);
        return ResponseEntity.ok(beneficiaryNo);
    }
}

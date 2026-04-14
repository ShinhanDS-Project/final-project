package com.merge.final_project.recipient.beneficiary.controller;

import com.merge.final_project.global.jwt.JwtTokenProvider;
import com.merge.final_project.recipient.beneficiary.dto.BeneficiaryInfoResponseDTO;
import com.merge.final_project.recipient.beneficiary.dto.BeneficiarySigninRequestDTO;
import com.merge.final_project.recipient.beneficiary.dto.BeneficiarySignupRequestDTO;
import com.merge.final_project.recipient.beneficiary.dto.BeneficiaryUpdateRequestDTO;
import com.merge.final_project.recipient.beneficiary.service.BeneficiaryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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

@io.swagger.v3.oas.annotations.tags.Tag(name = "수혜자", description = "수혜자 회원가입·로그인·정보 조회·수정 API")
@RestController
@RequestMapping("/api/v1/beneficiary")
@RequiredArgsConstructor
@Log4j2
public class BeneficiaryRestController {

    private final BeneficiaryService beneficiaryService;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;

    @Operation(summary = "수혜자 회원가입", description = "수혜자 계정을 생성합니다. 생성된 수혜자 번호를 반환합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "회원가입 성공 — 수혜자 번호 반환"),
            @ApiResponse(responseCode = "400", description = "요청 값 유효성 오류"),
            @ApiResponse(responseCode = "409", description = "이미 등록된 이메일")
    })
    @PostMapping("/signup")
    public ResponseEntity<Long> signup(@RequestBody BeneficiarySignupRequestDTO dto) {
        log.info("리액트 수혜자 회원가입 시도: {}", dto.getEmail());
        return ResponseEntity.ok(beneficiaryService.signup(dto));
    }

    @Operation(summary = "수혜자 로그인", description = "수혜자 이메일과 비밀번호로 로그인합니다. 액세스 토큰을 httpOnly 쿠키(accessToken)로 발급합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "로그인 성공 — accessToken 쿠키 발급"),
            @ApiResponse(responseCode = "401", description = "이메일 또는 비밀번호 불일치"),
            @ApiResponse(responseCode = "400", description = "요청 값 유효성 오류")
    })
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

    @Operation(summary = "내 정보 조회", description = "로그인한 수혜자의 상세 정보를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 요청")
    })
    @GetMapping("/me")
    public ResponseEntity<BeneficiaryInfoResponseDTO> getMyInfo(Principal principal) {
        return ResponseEntity.ok(beneficiaryService.getMyDetailInfo(principal.getName()));
    }

    @Operation(summary = "내 정보 수정", description = "로그인한 수혜자의 정보를 수정합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "수정 성공"),
            @ApiResponse(responseCode = "400", description = "요청 값 유효성 오류"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 요청")
    })
    @PutMapping("/me")
    public ResponseEntity<String> updateMyInfo(@RequestBody BeneficiaryUpdateRequestDTO dto, Principal principal) {
        beneficiaryService.updateMyInfo(principal.getName(), dto);
        return ResponseEntity.ok("success");
    }
}

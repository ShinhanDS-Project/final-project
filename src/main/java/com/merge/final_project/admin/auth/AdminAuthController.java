package com.merge.final_project.admin.auth;

import com.merge.final_project.admin.auth.dto.AdminSigninRequestDTO;
import com.merge.final_project.admin.auth.dto.AdminSigninResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "관리자 인증", description = "관리자 로그인/로그아웃 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/auth")
public class AdminAuthController {

    private final AdminAuthService adminAuthService;

    @Operation(summary = "관리자 로그인", description = "관리자 ID와 비밀번호로 로그인하여 JWT 액세스 토큰을 발급합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "로그인 성공 — JWT 토큰 반환"),
            @ApiResponse(responseCode = "401", description = "아이디 또는 비밀번호 불일치"),
            @ApiResponse(responseCode = "400", description = "요청 값 유효성 오류")
    })
    @PostMapping("/login")
    public ResponseEntity<AdminSigninResponseDTO> login(@RequestBody @Valid AdminSigninRequestDTO requestDTO) {
        AdminSigninResponseDTO response = adminAuthService.login(requestDTO);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "관리자 로그아웃", description = "Authorization 헤더의 Bearer 토큰을 무효화합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "로그아웃 성공"),
            @ApiResponse(responseCode = "401", description = "유효하지 않은 토큰")
    })
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @Parameter(description = "Bearer {accessToken}", required = true)
            @RequestHeader("Authorization") String bearerToken) {
        adminAuthService.logout(bearerToken);
        return ResponseEntity.ok().build();
    }
}

package com.merge.final_project.admin.auth;

import com.merge.final_project.admin.auth.dto.AdminSigninRequestDTO;
import com.merge.final_project.admin.auth.dto.AdminSigninResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/auth")
public class AdminAuthController {

    private final AdminAuthService adminAuthService;

    @PostMapping("/login")
    public ResponseEntity<AdminSigninResponseDTO> login(@RequestBody AdminSigninRequestDTO requestDTO) {
        AdminSigninResponseDTO response = adminAuthService.login(requestDTO);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestHeader("Authorization") String bearerToken) {
        adminAuthService.logout(bearerToken);
        return ResponseEntity.ok().build(); //로그아웃은 처리 성공여부만 응답
    }
}

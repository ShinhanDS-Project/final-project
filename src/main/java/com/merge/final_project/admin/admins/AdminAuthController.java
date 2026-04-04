package com.merge.final_project.admin.admins;

import com.merge.final_project.admin.admins.dto.AdminSigninRequestDTO;
import com.merge.final_project.admin.admins.dto.AdminSigninResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/auth")
public class AdminAuthController {

    private final AdminAuthService adminAuthService;

    @PostMapping("/login")
    public ResponseEntity<AdminSigninResponseDTO> login(@RequestBody AdminSigninRequestDTO requestDTO){
        AdminSigninResponseDTO response = adminAuthService.login(requestDTO);
        return ResponseEntity.ok(response);
    }
}

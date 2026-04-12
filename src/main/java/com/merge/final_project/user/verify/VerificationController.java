package com.merge.final_project.user.verify;

import com.merge.final_project.user.verify.dto.UserVerifyCodeRequestDTO;
import com.merge.final_project.user.verify.dto.UserVerifyRequestDTO;
import com.merge.final_project.user.verify.dto.UserVerifyResponseDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/api/auth")
@RestController
@RequiredArgsConstructor
public class VerificationController {

    private final VerificationService verificationService;

    @PostMapping("/users/verification/send")
    public ResponseEntity<UserVerifyResponseDTO> send(@Valid @RequestBody UserVerifyRequestDTO dto) {
        return ResponseEntity.ok(verificationService.sendVerificationCode(dto));
    }

    @PostMapping("/users/verification/verify")
    public boolean verify(@Valid @RequestBody UserVerifyCodeRequestDTO dto) {
        return verificationService.verifyCode(dto.getEmail(),dto.getCode());
    }

}
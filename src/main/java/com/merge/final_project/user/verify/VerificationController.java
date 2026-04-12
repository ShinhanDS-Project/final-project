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
    public ResponseEntity<UserVerifyResponseDTO> verify(@Valid @RequestBody UserVerifyCodeRequestDTO dto) {
        return ResponseEntity.ok(verificationService.verifyCode(dto));
    }

    // 비밀번호 재설정( 로그인 전 5번이상 로그인 실패시 재설정하게끔 유도)
    @PostMapping("/password/change")
    public void changePassword(){
        //  UserService.
    }

    // 비밀번호 변경
    @PostMapping("/password/edit")
    public void editPassword(){

    }
}
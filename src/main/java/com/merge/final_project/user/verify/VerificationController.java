package com.merge.final_project.user.verify;

import com.merge.final_project.user.verify.dto.UserVerifyCodeRequestDTO;
import com.merge.final_project.user.verify.dto.UserVerifyRequestDTO;
import com.merge.final_project.user.verify.dto.UserVerifyResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@io.swagger.v3.oas.annotations.tags.Tag(name = "이메일 인증", description = "회원가입 이메일 인증 코드 발송·확인 API")
@RequestMapping("/api/auth")
@RestController
@RequiredArgsConstructor
public class VerificationController {

    private final VerificationService verificationService;

    @Operation(summary = "이메일 인증 코드 발송", description = "회원가입 시 입력한 이메일로 인증 코드를 발송합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "발송 성공"),
            @ApiResponse(responseCode = "400", description = "요청 값 유효성 오류")
    })
    @PostMapping("/users/verification/send")
    public ResponseEntity<UserVerifyResponseDTO> send(@Valid @RequestBody UserVerifyRequestDTO dto) {
        return ResponseEntity.ok(verificationService.sendVerificationCode(dto));
    }

    @Operation(summary = "이메일 인증 코드 확인", description = "발송된 인증 코드가 올바른지 확인합니다. true=인증 성공, false=실패.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "확인 완료"),
            @ApiResponse(responseCode = "400", description = "요청 값 유효성 오류")
    })
    @PostMapping("/users/verification/verify")
    public boolean verify(@Valid @RequestBody UserVerifyCodeRequestDTO dto) {
        return verificationService.verifyCode(dto.getEmail(),dto.getCode());
    }

}

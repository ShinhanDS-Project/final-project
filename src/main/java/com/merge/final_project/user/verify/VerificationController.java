package com.merge.final_project.user.verify;

import com.merge.final_project.user.signUp.UserSignUpRepository;
import com.merge.final_project.user.signUp.dto.UserSignUpRequestDTO;
import com.merge.final_project.user.verify.dto.UserVerifyRequestDTO;
import com.merge.final_project.user.verify.dto.UserVerifyResponseDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.regex.Pattern;

@RequestMapping("/api/auth")
@RestController
@RequiredArgsConstructor
public class VerificationController {
    private final UserSignUpRepository userSignUpRepository;
    private final VerificationService verificationService;

    @PostMapping("/users/verification/send")
    public ResponseEntity<UserVerifyResponseDTO> send(@Valid @RequestBody UserVerifyRequestDTO dto) {
        if(dto.getEmail() == null||!Pattern.matches("^(.+)@(.+)$", dto.getEmail())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(UserVerifyResponseDTO.builder()
                    .success(false)
                    .available(false)
                    .message("이메일 형식이 올바르지 않습니다.")
                    .build());
        }
        //이메일 중복 여부 확인
        if(userSignUpRepository.existsByEmailAndLoginType(dto.getEmail(), dto.getLoginType())) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(UserVerifyResponseDTO.builder()
                            .success(false)
                            .available(false)
                            .message("이미 가입된 이메일입니다.")
                            .build());
    }
        //인증번호 발송하기
        try{
            verificationService.sendVerificationCode(dto.getEmail());
            return ResponseEntity.ok(UserVerifyResponseDTO.builder()
                    .success(true)
                    .available(true)
                    .message("인증번호가 발송되었습니다. 이메일을 확인해주세요")
                    .build());
        }
        catch(IllegalStateException e){
            //서비스 단에서 횟수 초과 또는 시간초과로 던지는 예외 처리
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(UserVerifyResponseDTO.builder()
                            .success(false)
                            .available(true)
                            .message(e.getMessage())
                            .build());
        }
        }

}

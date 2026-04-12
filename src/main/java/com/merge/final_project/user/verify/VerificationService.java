package com.merge.final_project.user.verify;

import com.merge.final_project.user.verify.dto.*;

public interface VerificationService {

    UserVerifyResponseDTO sendVerificationCode(UserVerifyRequestDTO dto); // 회원가입용

    void sendPasswordResetCode(String email); // 비밀번호 재설정용

    boolean verifyCode(String email, String code);

    boolean isVerifiedEmail(String email);

    void deleteVerification(String email);

}

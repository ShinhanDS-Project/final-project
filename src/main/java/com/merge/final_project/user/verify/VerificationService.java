package com.merge.final_project.user.verify;

public interface VerificationService {
    void sendVerificationCode(String email);

    void verifyCode(String email, String code);

    boolean isVerifiedEmail(String email);

    void deleteVerification(String email);
}

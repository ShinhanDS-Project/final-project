package com.merge.final_project.user.verify;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class VerificationServiceImpl implements VerificationService {
    @Autowired
    EmailVerificationRepository emailVerificationRepository;

    @Override
    public void sendVerificationCode(String email) {
        validateEmailDuplicated(email);

        String code=createVerificationCode();
        LocalDateTime expiredAt=LocalDateTime.now().plusMinutes(5);

        EmailVerification emailVerification=emailVerificationRepository.findByEmail(email)
    }

    @Override
    public void verifyCode(String email, String code) {
        //코드 확인-> expired 일자 확인, 또는 

    }

    @Override
    public boolean isVerifiedEmail(String email) {
        //가입전 확인
        return false;
    }

    @Override
    public void deleteVerification(String email) {
        //배치코드? 또는 삭제
        emailVerificationRepository.deleteByEmail(email);
    }
}

package com.merge.final_project.user;

import com.merge.final_project.user.verify.VerificationService;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class realVerificationTest {
    @Autowired
    private VerificationService verificationService;
//
//    @Test
//   // @Transactional // 👈 이 줄을 추가해 주세요! (org.springframework.transaction.annotation.Transactional)
//    @DisplayName("실제 이메일로 인증 메일 발송")
//    void sendRealVerificationEmailTest() {
//        //String email = "@gmail.com";
//
//        verificationService.deleteVerification(email);
//        verificationService.sendVerificationCode(email);
//
//        System.out.println("인증 메일 발송 완료");
//    }
}

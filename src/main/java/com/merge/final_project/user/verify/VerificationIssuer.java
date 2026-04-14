package com.merge.final_project.user.verify;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class VerificationIssuer {
    private final EmailVerificationRepository emailVerificationRepository;
    private final JavaMailSender mailSender;

    // 수정 포인트 1: 별도 클래스의 public 메서드로 분리하여 프록시가 작동하게 함
    // 수정 포인트 2: REQUIRES_NEW를 사용하여 메인 로직이 실패해도 발급 기록은 남도록 함
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void issue(String email, String subject, String code) {
        EmailVerification verification = emailVerificationRepository.findByEmail(email)
                .orElse(null);

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiredAt = now.plusMinutes(5);

        if (verification != null) {
            // 수정 포인트 3: 재발급 시 기존의 실패 횟수(attemptCount)를 0으로 초기화
            verification.updateVerification(code, expiredAt);
            verification.setRequestCount(verification.getRequestCount() + 1);
            verification.setAttemptCount(0);
            verification.setVerified(false);
        } else {
            verification = EmailVerification.builder()
                    .email(email)
                    .verificationCode(code)
                    .expiredAt(expiredAt)
                    .requestCount(1)
                    .attemptCount(0)
                    .verified(false)
                    .build();
        }

        emailVerificationRepository.save(verification);
        sendEmail(email, code, subject);
    }

    private void sendEmail(String to, String code, String subject) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText("인증 번호: " + code + "\n5분 이내에 입력해주세요.");
        mailSender.send(message);
    }
}

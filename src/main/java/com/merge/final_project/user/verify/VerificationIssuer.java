package com.merge.final_project.user.verify;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
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
    private final ApplicationEventPublisher eventPublisher; // 이벤트 발행기 추가

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void issue(String email, String subject, String code) {
        // 1. 비관적 락으로 조회 (정책 검사와 수정을 원자적으로 처리)
        EmailVerification verification = emailVerificationRepository.findByEmailForUpdate(email)
                .orElse(null);

        LocalDateTime now = LocalDateTime.now();

        if (verification != null) {
            // 2. 정책 검증 (락 안에서 수행하므로 동시 요청에 안전함)
            if (verification.getExpiredAt() != null && verification.getExpiredAt().isAfter(now.plusMinutes(4))) {
                throw new IllegalStateException("인증번호는 1분마다 재요청할 수 있습니다.");
            }
            if (verification.getRequestCount() >= 5) {
                throw new IllegalStateException("인증번호 요청 횟수(5회)를 초과했습니다.");
            }

            // 3. 상태 업데이트
            verification.updateVerification(code, now.plusMinutes(5));
            verification.setRequestCount(verification.getRequestCount() + 1);
            verification.setAttemptCount(0);
            verification.setVerified(false);
        } else {
            // 신규 생성
            verification = EmailVerification.builder()
                    .email(email)
                    .verificationCode(code)
                    .expiredAt(now.plusMinutes(5))
                    .requestCount(1)
                    .attemptCount(0)
                    .verified(false)
                    .build();
        }

        emailVerificationRepository.save(verification);

        // 4. 트랜잭션 커밋 성공 시에만 메일이 발송되도록 이벤트 발행
        eventPublisher.publishEvent(new VerificationEvent(email, subject, code));
    }
}

package com.merge.final_project.user.verify;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class VerificationIssuer {
    private final EmailVerificationRepository emailVerificationRepository;
    private final ApplicationEventPublisher eventPublisher; // 이벤트 발행기 추가

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void issue(String email, String subject, String code) {
        // [수정] List로 조회하여 NonUniqueResultException 에러를 방지합니다.
        List<EmailVerification> verifications = emailVerificationRepository.findByEmailForUpdate(email);

        EmailVerification verification;
        LocalDateTime now = LocalDateTime.now();

        if (!verifications.isEmpty()) {
            // 중복 데이터가 있다면 첫 번째 것만 사용하고 나머지는 삭제하여 정리합니다.
            verification = verifications.get(0);
            if (verifications.size() > 1) {
                emailVerificationRepository.deleteAllInBatch(verifications.subList(1, verifications.size()));
            }

            // 2. 정책 검증
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

package com.merge.final_project.user.verify;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class VerificationCleanupScheduler {

    private final EmailVerificationRepository emailVerificationRepository;

    public VerificationCleanupScheduler(EmailVerificationRepository emailVerificationRepository) {
        this.emailVerificationRepository = emailVerificationRepository;
    }

    @Scheduled(cron="0 0 0 * * *")
    @Transactional
    public void cleanup() {
        emailVerificationRepository.deleteAllByExpiredAtBefore(LocalDateTime.now());
        System.out.println("배치작업- 만료된 이메일 인증 데이터 초기화 완료");
    }
}

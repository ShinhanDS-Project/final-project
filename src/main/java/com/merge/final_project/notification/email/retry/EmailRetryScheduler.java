package com.merge.final_project.notification.email.retry;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class EmailRetryScheduler {

    private final EmailRetryService emailRetryService;

    // 매일 새벽 2시 — 전날 실패한 메일 재시도
    @Scheduled(cron = "0 0 2 * * *")
    public void retryFailedEmails() {
        log.info("메일 재시도 배치 시작");
        emailRetryService.retryFailed();
        log.info("메일 재시도 배치 완료");
    }
}

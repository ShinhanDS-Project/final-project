package com.merge.final_project.redemption.scheduler;

import com.merge.final_project.redemption.service.RedemptionRecoveryService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RedemptionRecoveryScheduler {

    private final RedemptionRecoveryService redemptionRecoveryService;

    // 일정 주기마다 정산 복구 로직 실행 (기본: 10분마다)
    @Scheduled(cron = "${blockchain.recovery.redemption-cron:0 */10 * * * *}")
    public void recoverRedemptionFinalization() {
        redemptionRecoveryService.recoverOnChainConfirmedRedemptions();
    }
}

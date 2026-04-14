package com.merge.final_project.campaign.settlement.scheduler;

import com.merge.final_project.campaign.settlement.service.SettlementRecoveryService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SettlementRecoveryScheduler {

    private final SettlementRecoveryService settlementRecoveryService;
    // 일정 주기마다 정산 복구 로직 실행 (기본: 10분마다)
    @Scheduled(cron = "${blockchain.recovery.settlement-cron:0 */10 * * * *}")
    public void recoverSettlementFinalization() {
        settlementRecoveryService.recoverOnChainConfirmedSettlements();
    }
}

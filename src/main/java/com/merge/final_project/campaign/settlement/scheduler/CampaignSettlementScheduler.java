package com.merge.final_project.campaign.settlement.scheduler;

import com.merge.final_project.campaign.settlement.service.CampaignSettlementService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CampaignSettlementScheduler {

    private final CampaignSettlementService campaignSettlementService;

    @Scheduled(cron = "0 0 * * * *")
    public void runSettlement() {
        campaignSettlementService.settleAll();
    }

}

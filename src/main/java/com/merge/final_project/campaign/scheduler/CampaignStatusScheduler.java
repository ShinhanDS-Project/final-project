package com.merge.final_project.campaign.scheduler;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CampaignStatusScheduler {

    private final CampaignStatusSchedulerService campaignStatusSchedulerService;

    //0시 0분 0초 매일
    @Scheduled(cron = "0 0 0 * * *")
    public void run() {
        campaignStatusSchedulerService.activateCampaigns();
        campaignStatusSchedulerService.endCampaigns();
    }
}

package com.merge.final_project.campaign.settlement.service;

import com.merge.final_project.blockchain.service.SettlementTransactionService;
import com.merge.final_project.campaign.campaigns.entity.Campaign;
import com.merge.final_project.campaign.campaigns.repository.CampaignRepository;
import com.merge.final_project.campaign.campaigns.CampaignStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CampaignSettlementService {

    private final CampaignRepository campaignRepository;
    private final SettlementTransactionService settlementTransactionService;

    //    캠페인 조회
    public void settleAll() {
        List<Campaign> campaigns =
                campaignRepository.findByCampaignStatus(CampaignStatus.ENDED);

        for(Campaign campaign : campaigns) {
            try {
                settlementTransactionService.processSettlement(campaign);
            } catch (Exception e){
                log.error("정산 실패 campaignNo={}", campaign.getCampaignNo(), e);
            }
        }
    }

}

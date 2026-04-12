package com.merge.final_project.campaign.scheduler;

import com.merge.final_project.campaign.campaigns.CampaignStatus;
import com.merge.final_project.campaign.campaigns.entity.Campaign;
import com.merge.final_project.campaign.campaigns.repository.CampaignRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CampaignStatusSchedulerService {

    private final CampaignRepository campaignRepository;

    //모금 시작하게 변경하는 메서드 (recruiting -> active로)
    //모금 시작일 당일 00:00시 기준으로 승인된 상태면 캠페인 진행 상태 변경
    @Transactional
    public void activateCampaigns(){
        LocalDate today = LocalDate.now();
        List<Campaign> list = campaignRepository.findByCampaignStatus(CampaignStatus.RECRUITING);

        for (Campaign campaign : list) {
            //캠페인 상태가 모집중(승인완료)인 상황에서 시작일이 null이 아니면서 당일일 때 상태 변경 배치 시작
            if (campaign.getStartAt() != null
                    && !campaign.getStartAt().toLocalDate().isAfter(today)) {
                campaign.active();
                log.info("RECRUITING -> ACTIVE campaignNo={}", campaign.getCampaignNo());

            }
        }
    }

    //캠페인 모금 종료 처리하는 메서드. 종료일 하루동안 기부 받고, 다음날 00:00시 정시 되면 ENDED로 변경되게 할 것임.
    //ACTIVE -> ENDED
    @Transactional
    public void endCampaigns() {
        LocalDate today = LocalDate.now();
        List<Campaign> list = campaignRepository.findByCampaignStatus(CampaignStatus.ACTIVE);

        for (Campaign campaign : list) {
            if (campaign.getEndAt() != null
                    && campaign.getEndAt().toLocalDate().isBefore(today)) {
                campaign.ended();
                log.info("ACTIVE -> ENDED campaignNo={}", campaign.getCampaignNo());
            }
        }
    }
}

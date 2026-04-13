package com.merge.final_project.campaign.settlement.Repository;

import com.merge.final_project.campaign.campaigns.entity.Campaign;
import com.merge.final_project.campaign.settlement.Settlement;
import com.merge.final_project.campaign.settlement.SettlementStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SettlementRepository extends JpaRepository<Settlement, Long> {
    boolean existsByCampaignAndStatusIn(Campaign campaign, List<SettlementStatus> statuses);

    List<Settlement> findByCampaign(Campaign campaign);

    //findBy
    List<Settlement> findByCampaignNo(Long campaignNo);
}

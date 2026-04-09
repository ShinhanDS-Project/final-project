package com.merge.final_project.campaign.campaigns.repository;

import com.merge.final_project.campaign.campaigns.CampaignStatus;
import com.merge.final_project.campaign.campaigns.entity.Campaign;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CampaignRepository extends JpaRepository<Campaign, Long> {
    List<Campaign> findByBeneficiaryNo(Long beneficiaryNo);

    boolean existsByCampaignNoAndBeneficiaryNo(Long campaignNo, Long beneficiaryNo);

    List<Campaign> findByCampaignStatus(CampaignStatus campaignStatus);

    @EntityGraph(attributePaths = "foundation")
    List<Campaign> findByCampaignStatusOrderByEndAtAscCampaignNoDesc(CampaignStatus campaignStatus);

    @EntityGraph(attributePaths = "foundation")
    List<Campaign> findByCampaignStatusOrderByCurrentAmountDescCampaignNoDesc(CampaignStatus campaignStatus);
}

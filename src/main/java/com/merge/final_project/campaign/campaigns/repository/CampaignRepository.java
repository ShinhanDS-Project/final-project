package com.merge.final_project.campaign.campaigns.repository;

import com.merge.final_project.campaign.campaigns.CampaignStatus;
import com.merge.final_project.campaign.campaigns.entity.Campaign;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CampaignRepository extends JpaRepository<Campaign, Long> {
    List<Campaign> findByBeneficiaryNo(Long beneficiaryNo);

    boolean existsByCampaignNoAndBeneficiaryNo(Long campaignNo, Long beneficiaryNo);
    List<Campaign> findByCampaignStatus(CampaignStatus campaignStatus);
}

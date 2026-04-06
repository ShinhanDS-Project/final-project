package com.merge.final_project.campaign.campaigns;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CampaignRepository extends JpaRepository<Campaign, Long> {
    List<Campaign> findByCampaignStatus(CampaignStatus campaignStatus);
}

package com.merge.final_project.campaign.campaigns.repository;

import com.merge.final_project.campaign.campaigns.entity.Campaign;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CampaignRepository extends JpaRepository<Campaign, Integer> {
}

package com.merge.final_project.campaign.campaigns.repository;

import com.merge.final_project.campaign.campaigns.entity.Campaign;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CampaignRepository extends JpaRepository<Campaign, Long> {
    // 캠페인 엔티티 관리 저장소
}

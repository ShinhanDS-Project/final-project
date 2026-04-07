package com.merge.final_project.campaign.campaigns.repository;

import com.merge.final_project.campaign.campaigns.ApprovalStatus;
import com.merge.final_project.campaign.campaigns.CampaignStatus;
import com.merge.final_project.campaign.campaigns.entity.Campaign;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CampaignRepository extends JpaRepository<Campaign, Long> {
    @EntityGraph(attributePaths = "foundation")
    List<Campaign> findByApprovalStatusOrderByEndAtAscCampaignNoDesc(ApprovalStatus approvalStatus);

    @EntityGraph(attributePaths = "foundation")
    List<Campaign> findByApprovalStatusOrderByCurrentAmountDescCampaignNoDesc(ApprovalStatus approvalStatus);

    List<Campaign> findByCampaignStatus(CampaignStatus campaignStatus);
}

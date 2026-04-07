package com.merge.final_project.campaign.campaigns.repository;

import com.merge.final_project.campaign.campaigns.ApprovalStatus;
import com.merge.final_project.campaign.campaigns.entity.Campaign;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CampaignRepository extends JpaRepository<Campaign, Long> {
    // 마감 임박순 조회
    @EntityGraph(attributePaths = "foundation")
    List<Campaign> findByApprovalStatusOrderByEndAtAscCampaignNoDesc(ApprovalStatus approvalStatus);

    // 모금액 많은 순 조회
    @EntityGraph(attributePaths = "foundation")
    List<Campaign> findByApprovalStatusOrderByCurrentAmountDescCampaignNoDesc(ApprovalStatus approvalStatus);
}

package com.merge.final_project.campaign.campaigns.repository;

import com.merge.final_project.campaign.campaigns.ApprovalStatus;
import com.merge.final_project.campaign.campaigns.CampaignStatus;
import com.merge.final_project.campaign.campaigns.entity.Campaign;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CampaignRepository extends JpaRepository<Campaign, Long> {
    List<Campaign> findByBeneficiaryNo(Long beneficiaryNo);

    boolean existsByCampaignNoAndBeneficiaryNo(Long campaignNo, Long beneficiaryNo);
    List<Campaign> findByCampaignStatus(CampaignStatus campaignStatus);

    // [가빈] 승인 상태별 캠페인 목록 조회 (PENDING, REJECTED 등)
    Page<Campaign> findByApprovalStatus(ApprovalStatus approvalStatus, Pageable pageable);
}

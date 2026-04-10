package com.merge.final_project.campaign.campaigns.repository;

import com.merge.final_project.campaign.campaigns.ApprovalStatus;
import com.merge.final_project.campaign.campaigns.CampaignStatus;
import com.merge.final_project.campaign.campaigns.dto.CampaignListResponseDTO;
import com.merge.final_project.campaign.campaigns.entity.Campaign;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface CampaignRepository extends JpaRepository<Campaign, Long> {
    /**
     * 수혜자 기준 캠페인 목록 조회.
     */
    List<Campaign> findByBeneficiaryNo(Long beneficiaryNo);

    /**
     * 수혜자-캠페인 소유 관계 존재 여부 확인.
     */
    boolean existsByCampaignNoAndBeneficiaryNo(Long campaignNo, Long beneficiaryNo);

    /**
     * 캠페인 상태별 목록 조회.
     */
    List<Campaign> findByCampaignStatus(CampaignStatus campaignStatus);

    /**
     * wallet_no로 캠페인 단건 조회.
     * 지갑 상세 -> 캠페인명 역추적에 사용한다.
     */
    Optional<Campaign> findByWalletNo(Long walletNo);

    /**
     * 캠페인 제목 대소문자 무시 단건 조회.
     * 대시보드 검색어 해석(resolve) 단계에서 사용한다.
     */
    Optional<Campaign> findFirstByTitleIgnoreCase(String title);

    /**
     * 다수 wallet_no에 매핑된 캠페인 목록 조회.
     */
    List<Campaign> findByWalletNoIn(Collection<Long> walletNos);

    // [가빈] 승인 상태별 캠페인 목록 조회 (PENDING, REJECTED 등)
    Page<Campaign> findByApprovalStatus(ApprovalStatus approvalStatus, Pageable pageable);
    //[가빈] 기부단체 별 캠페인 목록 조회
    Page<CampaignListResponseDTO> findByFoundationNo(Long foundationNo, Pageable pageable);
}

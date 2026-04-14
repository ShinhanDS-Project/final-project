package com.merge.final_project.campaign.settlement.Repository;

import com.merge.final_project.campaign.campaigns.entity.Campaign;
import com.merge.final_project.campaign.settlement.Settlement;
import com.merge.final_project.campaign.settlement.SettlementStatus;
import org.springframework.data.domain.Page; // [가빈] 추가
import org.springframework.data.domain.Pageable; // [가빈] 추가
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SettlementRepository extends JpaRepository<Settlement, Long> {
    boolean existsByCampaignAndStatusIn(Campaign campaign, List<SettlementStatus> statuses);

    List<Settlement> findByCampaign(Campaign campaign);

    //findBy => [가빈] 채원 코드 순서만 변경
    List<Settlement> findByCampaign_CampaignNo(Long campaignNo);

    // [가빈] 기부단체 마이페이지 — 본인 정산 내역 조회
    Page<Settlement> findByFoundationNo(Long foundationNo, Pageable pageable);
}

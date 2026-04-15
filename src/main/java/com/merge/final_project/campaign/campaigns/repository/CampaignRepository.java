package com.merge.final_project.campaign.campaigns.repository;

import com.merge.final_project.campaign.campaigns.ApprovalStatus;
import com.merge.final_project.campaign.campaigns.CampaignStatus;
import com.merge.final_project.campaign.campaigns.entity.Campaign;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
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

    @EntityGraph(attributePaths = "foundation")
    List<Campaign> findByCampaignStatusOrderByEndAtAscCampaignNoDesc(CampaignStatus campaignStatus);

    @EntityGraph(attributePaths = "foundation")
    List<Campaign> findByCampaignStatusOrderByCurrentAmountDescCampaignNoDesc(CampaignStatus campaignStatus);

    @EntityGraph(attributePaths = "foundation")
    List<Campaign> findByCampaignStatusInOrderByEndAtAscCampaignNoDesc(Collection<CampaignStatus> campaignStatuses);

    @EntityGraph(attributePaths = "foundation")
    List<Campaign> findByCampaignStatusInOrderByCurrentAmountDescCampaignNoDesc(Collection<CampaignStatus> campaignStatuses);

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

    // [가빈] 관리자 캠페인 목록 — 승인 상태 + 키워드(제목) 검색
    // keyword null → "" 변환 후 호출 (IS NULL 대신 = '' 사용: PostgreSQL lower(bytea) 오류 방지)
    @Query("SELECT c FROM Campaign c WHERE c.approvalStatus = :approvalStatus AND (:keyword = '' OR LOWER(c.title) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Campaign> findByApprovalStatusWithKeyword(@Param("approvalStatus") ApprovalStatus approvalStatus, @Param("keyword") String keyword, Pageable pageable);
    //[가빈] 기부단체 별 캠페인 목록 조회
    Page<Campaign> findByFoundationNo(Long foundationNo, Pageable pageable);

    // [가빈] 기부단체 마이페이지 — 상태 필터 포함 캠페인 목록
    Page<Campaign> findByFoundationNoAndCampaignStatus(Long foundationNo, CampaignStatus campaignStatus, Pageable pageable);

    // [가빈] 기부단체 마이페이지 — 진행 중인 캠페인 수
    long countByFoundationNoAndCampaignStatus(Long foundationNo, CampaignStatus campaignStatus);

    // [가빈] 기부단체 마이페이지 — 키워드(제목) + 상태 필터 통합 쿼리
    @Query("SELECT c FROM Campaign c WHERE c.foundationNo = :foundationNo AND (:campaignStatus IS NULL OR c.campaignStatus = :campaignStatus) AND (:keyword IS NULL OR LOWER(c.title) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Campaign> findByFoundationNoWithFilter(@Param("foundationNo") Long foundationNo, @Param("campaignStatus") CampaignStatus campaignStatus, @Param("keyword") String keyword, Pageable pageable);



    // [가빈] SETTLED + usage_end_at 경과 + 보고서 미제출 캠페인 조회
    // cutoffDate = today - 7일 or today - 14일 (서비스에서 계산해서 넘김)
    @Query(value = """
        SELECT c.* FROM campaign c
        WHERE c.campaign_status = 'SETTLED'
        AND c.usage_end_at < :cutoffDate
        AND NOT EXISTS (
            SELECT 1 FROM final_report f WHERE f.campaign_no = c.campaign_no
        )
        """, nativeQuery = true)
    List<Campaign> findSettledCampaignsWithNoReport(@Param("cutoffDate") LocalDateTime cutoffDate);


    Optional<Campaign> findByCampaignNo(Long campaignNo);

    Optional<Campaign> findByCampaignNoAndApprovalStatus(Long campaignNo, ApprovalStatus approvalStatus);

    // [가빈] 대시보드용 카운트
    long countByCampaignStatus(CampaignStatus campaignStatus);

    long countByApprovalStatus(ApprovalStatus approvalStatus);

    @Query("SELECT COUNT(c) FROM Campaign c WHERE c.approvalStatus = :approvalStatus AND c.currentAmount >= c.targetAmount")
    long countAchievedCampaigns(@Param("approvalStatus") ApprovalStatus approvalStatus);

    // [가빈] 카테고리별 캠페인 수 (승인된 것만)
    // Object[] = [category(String), count(Long)]
    @Query("SELECT c.category, COUNT(c) FROM Campaign c WHERE c.approvalStatus = :approvalStatus GROUP BY c.category")
    List<Object[]> countCampaignByCategory(@Param("approvalStatus") ApprovalStatus approvalStatus);

    //[채원]
    List<Campaign> findAllByCampaignNoIn(List<Long> campaignNos);

    //[바다] main 사용 캠페인
    Page<Campaign> findByCampaignStatusInAndApprovalStatusOrderByEndAtAscCampaignNoDesc(
            Collection<CampaignStatus> campaignStatuses,
            ApprovalStatus approvalStatus,
            Pageable pageable
    );

    //[바다] main 사용 캠페인
    Page<Campaign> findByCampaignStatusInAndApprovalStatusOrderByCurrentAmountDescCampaignNoDesc(
            Collection<CampaignStatus> campaignStatuses,
            ApprovalStatus approvalStatus,
            Pageable pageable
    );


}

package com.merge.final_project.campaign;

import com.merge.final_project.campaign.campaigns.ApprovalStatus;
import com.merge.final_project.campaign.campaigns.CampaignStatus;
import com.merge.final_project.campaign.campaigns.entity.Campaign;
import com.merge.final_project.campaign.campaigns.repository.CampaignRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime; //가빈 추가

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class CampaignRepositoryTest {

    @Autowired
    private CampaignRepository campaignRepository;

    // 테스트용 캠페인 생성 헬퍼
    private Campaign campaign(ApprovalStatus approvalStatus) {
        // Campaign 엔티티의 created_at은 @CreatedDate 없이 직접 선언되어 있어 수동 세팅 필요
        return Campaign.builder()
                .title("테스트 캠페인")
                .foundationNo(1L)
                .approvalStatus(approvalStatus)
                .campaignStatus(CampaignStatus.PENDING)
                .targetAmount(1000000L)
                .currentAmount(0L)
                .createdAt(LocalDateTime.now()) //가빈 ->// Campaign 엔티티의 created_at은 @CreatedDate 없이 직접 선언되어 있어 수동 세팅 필요
                .build();
    }

    @Test
    @DisplayName("PENDING 상태 캠페인만 조회된다")
    void PENDING_캠페인_조회() {
        // PENDING 2개, APPROVED 1개 저장
        campaignRepository.save(campaign(ApprovalStatus.PENDING));
        campaignRepository.save(campaign(ApprovalStatus.PENDING));
        campaignRepository.save(campaign(ApprovalStatus.APPROVED));

        Page<Campaign> result = campaignRepository
                .findByApprovalStatus(ApprovalStatus.PENDING, PageRequest.of(0, 10));

        // PENDING 상태만 반환되는지 검증
        assertThat(result.getContent()).allMatch(c -> c.getApprovalStatus() == ApprovalStatus.PENDING);
        assertThat(result.getContent()).hasSize(2);
    }

    @Test
    @DisplayName("REJECTED 상태 캠페인만 조회된다")
    void REJECTED_캠페인_조회() {
        // REJECTED 1개, PENDING 1개, APPROVED 1개 저장
        campaignRepository.save(campaign(ApprovalStatus.REJECTED));
        campaignRepository.save(campaign(ApprovalStatus.PENDING));
        campaignRepository.save(campaign(ApprovalStatus.APPROVED));

        Page<Campaign> result = campaignRepository
                .findByApprovalStatus(ApprovalStatus.REJECTED, PageRequest.of(0, 10));

        // REJECTED 상태만 반환되는지 검증
        assertThat(result.getContent()).allMatch(c -> c.getApprovalStatus() == ApprovalStatus.REJECTED);
        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    @DisplayName("APPROVED 상태는 PENDING 조회 결과에 포함되지 않는다")
    void APPROVED_캠페인은_PENDING_조회에_미포함() {
        // APPROVED만 저장
        campaignRepository.save(campaign(ApprovalStatus.APPROVED));

        Page<Campaign> result = campaignRepository
                .findByApprovalStatus(ApprovalStatus.PENDING, PageRequest.of(0, 10));

        assertThat(result.getContent()).isEmpty();
    }
}

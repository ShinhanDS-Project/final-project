package com.merge.final_project.campaign.campaigns;

import com.merge.final_project.campaign.campaigns.entity.Campaign;
import com.merge.final_project.campaign.campaigns.repository.CampaignRepository;
import com.merge.final_project.report.finalreport.ReportApprovalStatus;
import com.merge.final_project.report.finalreport.entitiy.FinalReport;
import com.merge.final_project.report.finalreport.repository.FinalReportRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class CampaignBatchRepositoryTest {

    @Autowired
    private CampaignRepository campaignRepository;

    @Autowired
    private FinalReportRepository finalReportRepository;

    @BeforeEach
    void setUp() {
        finalReportRepository.deleteAll();
        campaignRepository.deleteAll();
    }

    // 테스트용 SETTLED 캠페인 생성 헬퍼
    private Campaign settledCampaign(LocalDateTime usageEndAt) {
        return Campaign.builder()
                .title("테스트 캠페인")
                .foundationNo(1L)
                .beneficiaryNo(1L)
                .approvalStatus(ApprovalStatus.APPROVED)
                .campaignStatus(CampaignStatus.SETTLED)
                .targetAmount(1000000L)
                .currentAmount(new BigDecimal("1000000"))
                .usageEndAt(usageEndAt)
                .build();
    }

    // 테스트용 보고서 생성 헬퍼
    private FinalReport report(Long campaignNo) {
        return FinalReport.builder()
                .title("활동 보고서")
                .content("활동 내용")
                .imagePath("test/image.jpg")
                .usagePurpose("test")
                .settlementNo(1L)
                .beneficiary_no(1L)
                .campaign_no(campaignNo)
                .key_no(1L)
                .approvalStatus(ReportApprovalStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .dueAt(LocalDateTime.now().plusDays(30))
                .build();
    }

    // ===================== 7일 경과 조회 =====================

    @Test
    @DisplayName("SETTLED + usage_end_at이 cutoff보다 이전 + 보고서 미제출 → 조회된다")
    void 기본_독촉_대상_조회() {
        // usage_end_at = 10일 전 → cutoff(7일 전 자정)보다 이전이므로 대상
        Campaign campaign = campaignRepository.save(
                settledCampaign(LocalDate.now().minusDays(10).atStartOfDay())
        );

        LocalDateTime cutoff7 = LocalDate.now().minusDays(7).atStartOfDay();
        List<Campaign> result = campaignRepository.findSettledCampaignsWithNoReport(cutoff7);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCampaignNo()).isEqualTo(campaign.getCampaignNo());
    }

    @Test
    @DisplayName("usage_end_at이 cutoff와 같으면 조회되지 않는다 (경계값: < 조건)")
    void 경계값_cutoff_당일_미조회() {
        // usage_end_at = 정확히 cutoff(7일 전 자정) → < 조건이라 미포함
        LocalDateTime cutoff7 = LocalDate.now().minusDays(7).atStartOfDay();
        campaignRepository.save(settledCampaign(cutoff7));

        List<Campaign> result = campaignRepository.findSettledCampaignsWithNoReport(cutoff7);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("usage_end_at이 cutoff보다 최근이면 조회되지 않는다")
    void usage_end_at_미경과_미조회() {
        // usage_end_at = 3일 전 → 7일 경과 안 됨
        campaignRepository.save(
                settledCampaign(LocalDate.now().minusDays(3).atStartOfDay())
        );

        LocalDateTime cutoff7 = LocalDate.now().minusDays(7).atStartOfDay();
        List<Campaign> result = campaignRepository.findSettledCampaignsWithNoReport(cutoff7);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("보고서가 이미 제출된 캠페인은 조회되지 않는다")
    void 보고서_제출_완료_미조회() {
        Campaign campaign = campaignRepository.save(
                settledCampaign(LocalDate.now().minusDays(10).atStartOfDay())
        );
        // 해당 캠페인에 보고서 존재
        finalReportRepository.save(report(campaign.getCampaignNo()));

        LocalDateTime cutoff7 = LocalDate.now().minusDays(7).atStartOfDay();
        List<Campaign> result = campaignRepository.findSettledCampaignsWithNoReport(cutoff7);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("SETTLED이 아닌 캠페인은 조회되지 않는다")
    void SETTLED_아닌_캠페인_미조회() {
        Campaign active = Campaign.builder()
                .title("진행중 캠페인")
                .foundationNo(1L)
                .beneficiaryNo(1L)
                .approvalStatus(ApprovalStatus.APPROVED)
                .campaignStatus(CampaignStatus.ACTIVE)  // ACTIVE
                .targetAmount(1000000L)
                .currentAmount(BigDecimal.ZERO)
                .usageEndAt(LocalDate.now().minusDays(10).atStartOfDay())
                .build();
        campaignRepository.save(active);

        LocalDateTime cutoff7 = LocalDate.now().minusDays(7).atStartOfDay();
        List<Campaign> result = campaignRepository.findSettledCampaignsWithNoReport(cutoff7);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("보고서 미제출 캠페인과 제출 캠페인이 섞여 있으면 미제출만 조회된다")
    void 혼합_데이터_미제출만_조회() {
        // 미제출 캠페인 2개
        Campaign unsubmitted1 = campaignRepository.save(
                settledCampaign(LocalDate.now().minusDays(10).atStartOfDay())
        );
        Campaign unsubmitted2 = campaignRepository.save(
                settledCampaign(LocalDate.now().minusDays(20).atStartOfDay())
        );
        // 제출 완료 캠페인 1개
        Campaign submitted = campaignRepository.save(
                settledCampaign(LocalDate.now().minusDays(15).atStartOfDay())
        );
        finalReportRepository.save(report(submitted.getCampaignNo()));

        LocalDateTime cutoff7 = LocalDate.now().minusDays(7).atStartOfDay();
        List<Campaign> result = campaignRepository.findSettledCampaignsWithNoReport(cutoff7);

        assertThat(result).hasSize(2);
        assertThat(result).extracting(Campaign::getCampaignNo)
                .containsExactlyInAnyOrder(unsubmitted1.getCampaignNo(), unsubmitted2.getCampaignNo());
    }

    @Test
    @DisplayName("14일 cutoff는 7일 cutoff보다 더 적은 캠페인을 반환한다")
    void cutoff_14일이_7일보다_범위_좁음() {
        // 10일 전 종료 → 7일 대상 O, 14일 대상 X
        campaignRepository.save(settledCampaign(LocalDate.now().minusDays(10).atStartOfDay()));
        // 20일 전 종료 → 7일 대상 O, 14일 대상 O
        campaignRepository.save(settledCampaign(LocalDate.now().minusDays(20).atStartOfDay()));

        LocalDateTime cutoff7  = LocalDate.now().minusDays(7).atStartOfDay();
        LocalDateTime cutoff14 = LocalDate.now().minusDays(14).atStartOfDay();

        List<Campaign> over7  = campaignRepository.findSettledCampaignsWithNoReport(cutoff7);
        List<Campaign> over14 = campaignRepository.findSettledCampaignsWithNoReport(cutoff14);

        assertThat(over7).hasSize(2);
        assertThat(over14).hasSize(1);
    }
}

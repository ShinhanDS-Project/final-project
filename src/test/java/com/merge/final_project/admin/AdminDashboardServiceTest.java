package com.merge.final_project.admin;

import com.merge.final_project.admin.adminlog.AdminLog;
import com.merge.final_project.admin.adminlog.AdminLogRepository;
import com.merge.final_project.admin.dashboard.AdminDashboardServiceImpl;
import com.merge.final_project.admin.dashboard.dto.CategoryRatioDTO;
import com.merge.final_project.admin.dashboard.dto.DashboardSummaryDTO;
import com.merge.final_project.admin.dashboard.dto.DonationTrendDTO;
import com.merge.final_project.campaign.campaigns.ApprovalStatus;
import com.merge.final_project.campaign.campaigns.CampaignStatus;
import com.merge.final_project.campaign.campaigns.repository.CampaignRepository;
import com.merge.final_project.donation.payment.PaymentRepository;
import com.merge.final_project.org.FoundationRepository;
import com.merge.final_project.org.ReviewStatus;
import com.merge.final_project.user.users.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminDashboardServiceTest {

    @Mock PaymentRepository paymentRepository;
    @Mock CampaignRepository campaignRepository;
    @Mock FoundationRepository foundationRepository;
    @Mock UserRepository userRepository;
    @Mock AdminLogRepository adminLogRepository;

    @InjectMocks
    AdminDashboardServiceImpl adminDashboardService;

    @Test
    @DisplayName("대시보드_요약_정상조회")
    void 대시보드_요약_정상조회() {
        when(paymentRepository.sumTodayCompletedAmount()).thenReturn(new BigDecimal("50000"));
        when(paymentRepository.sumTotalCompletedAmount()).thenReturn(new BigDecimal("1000000"));
        when(campaignRepository.countByCampaignStatus(CampaignStatus.ACTIVE)).thenReturn(5L);
        when(foundationRepository.countByReviewStatusNotIn(
                List.of(ReviewStatus.APPROVED, ReviewStatus.REJECTED))).thenReturn(3L);
        when(campaignRepository.countByApprovalStatus(ApprovalStatus.APPROVED)).thenReturn(20L);
        when(campaignRepository.countAchievedCampaigns(ApprovalStatus.APPROVED)).thenReturn(8L);
        when(userRepository.count()).thenReturn(150L);

        DashboardSummaryDTO result = adminDashboardService.getSummary();

        assertThat(result.getTodayDonationAmount()).isEqualByComparingTo("50000");
        assertThat(result.getTotalDonationAmount()).isEqualByComparingTo("1000000");
        assertThat(result.getActiveCampaignCount()).isEqualTo(5L);
        assertThat(result.getPendingFoundationCount()).isEqualTo(3L);
        assertThat(result.getAchievedCampaignRatio()).isEqualTo(40.0);
        assertThat(result.getTotalUserCount()).isEqualTo(150L);
    }

    @Test
    @DisplayName("대시보드_요약_기부액null이면_0반환")
    void 대시보드_요약_기부액null이면_0반환() {
        when(paymentRepository.sumTodayCompletedAmount()).thenReturn(null);
        when(paymentRepository.sumTotalCompletedAmount()).thenReturn(null);
        when(campaignRepository.countByCampaignStatus(CampaignStatus.ACTIVE)).thenReturn(0L);
        when(foundationRepository.countByReviewStatusNotIn(any())).thenReturn(0L);
        when(campaignRepository.countByApprovalStatus(ApprovalStatus.APPROVED)).thenReturn(0L);
        when(userRepository.count()).thenReturn(0L);

        DashboardSummaryDTO result = adminDashboardService.getSummary();

        assertThat(result.getTodayDonationAmount()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(result.getTotalDonationAmount()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(result.getAchievedCampaignRatio()).isEqualTo(0.0);
    }

    @Test
    @DisplayName("기부금_추이_정상조회")
    void 기부금_추이_정상조회() {
        Object[] row1 = new Object[]{"2026-04-12", new BigDecimal("30000")};
        Object[] row2 = new Object[]{"2026-04-13", new BigDecimal("50000")};
        when(paymentRepository.findDailyDonationTrend(any())).thenReturn(List.of(row1, row2));

        List<DonationTrendDTO> result = adminDashboardService.getDonationTrend(14);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getDate()).isEqualTo("2026-04-12");
        assertThat(result.get(0).getAmount()).isEqualByComparingTo("30000");
        assertThat(result.get(1).getDate()).isEqualTo("2026-04-13");
        assertThat(result.get(1).getAmount()).isEqualByComparingTo("50000");
    }

    @Test
    @DisplayName("기부금_추이_데이터없으면_빈리스트반환")
    void 기부금_추이_데이터없으면_빈리스트반환() {
        when(paymentRepository.findDailyDonationTrend(any())).thenReturn(List.of());

        List<DonationTrendDTO> result = adminDashboardService.getDonationTrend(7);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("카테고리별_비중_정상조회")
    void 카테고리별_비중_정상조회() {
        when(campaignRepository.countCampaignByCategory(ApprovalStatus.APPROVED))
                .thenReturn(List.<Object[]>of(new Object[]{"CHILD_YOUTH", 10L}));

        when(paymentRepository.findDonationAmountByCategory())
                .thenReturn(List.<Object[]>of(new Object[]{"CHILD_YOUTH", new BigDecimal("200000")}));

        List<CategoryRatioDTO> result = adminDashboardService.getCategoryRatio();

        // 전체 카테고리(6개) 모두 포함
        assertThat(result).hasSize(6);

        CategoryRatioDTO childYouth = result.stream()
                .filter(r -> "CHILD_YOUTH".equals(r.getCategory()))
                .findFirst().orElseThrow();
        assertThat(childYouth.getCampaignCount()).isEqualTo(10L);
        assertThat(childYouth.getDonationAmount()).isEqualByComparingTo("200000");
        assertThat(childYouth.getCategoryLabel()).isEqualTo("아동/청소년");
    }

    @Test
    @DisplayName("카테고리별_비중_데이터없는_카테고리는_0으로채움")
    void 카테고리별_비중_데이터없는_카테고리는_0으로채움() {
        when(campaignRepository.countCampaignByCategory(ApprovalStatus.APPROVED))
                .thenReturn(List.of());
        when(paymentRepository.findDonationAmountByCategory())
                .thenReturn(List.of());

        List<CategoryRatioDTO> result = adminDashboardService.getCategoryRatio();

        assertThat(result).hasSize(6);
        result.forEach(r -> {
            assertThat(r.getCampaignCount()).isEqualTo(0L);
            assertThat(r.getDonationAmount()).isEqualByComparingTo(BigDecimal.ZERO);
        });
    }

    @Test
    @DisplayName("최근_활동_로그_페이징_조회")
    void 최근_활동_로그_페이징_조회() {
        Page<AdminLog> page = new PageImpl<>(List.of());
        when(adminLogRepository.findAll(any(PageRequest.class))).thenReturn(page);

        var result = adminDashboardService.getRecentLogs(PageRequest.of(0, 10));

        assertThat(result).isNotNull();
        assertThat(result.getContent()).isEmpty();
    }
}

package com.merge.final_project.admin.dashboard;

import com.merge.final_project.admin.adminlog.AdminLog;
import com.merge.final_project.admin.adminlog.AdminLogRepository;
import com.merge.final_project.admin.adminlog.AdminLogResponseDTO;
import com.merge.final_project.admin.dashboard.dto.CategoryRatioDTO;
import com.merge.final_project.admin.dashboard.dto.DashboardSummaryDTO;
import com.merge.final_project.admin.dashboard.dto.DonationTrendDTO;
import com.merge.final_project.admin.dashboard.dto.UserRegistrationTrendDTO;
import com.merge.final_project.campaign.campaigns.ApprovalStatus;
import com.merge.final_project.campaign.campaigns.CampaignCategory;
import com.merge.final_project.campaign.campaigns.CampaignStatus;
import com.merge.final_project.campaign.campaigns.repository.CampaignRepository;
import com.merge.final_project.donation.payment.PaymentRepository;
import com.merge.final_project.org.FoundationRepository;
import com.merge.final_project.org.ReviewStatus;
import com.merge.final_project.user.users.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminDashboardServiceImpl implements AdminDashboardService {

    private final PaymentRepository paymentRepository;
    private final CampaignRepository campaignRepository;
    private final FoundationRepository foundationRepository;
    private final UserRepository userRepository;
    private final AdminLogRepository adminLogRepository;

    @Override
    @Transactional(readOnly = true)
    public DashboardSummaryDTO getSummary() {
        BigDecimal todayDonation = paymentRepository.sumTodayCompletedAmount();
        BigDecimal totalDonation = paymentRepository.sumTotalCompletedAmount();

        long activeCampaignCount = campaignRepository.countByCampaignStatus(CampaignStatus.ACTIVE);
        long endedCampaignCount = campaignRepository.countByCampaignStatus(CampaignStatus.ENDED);
        long settledCampaignCount = campaignRepository.countByCampaignStatus(CampaignStatus.SETTLED);

        long pendingFoundationCount = foundationRepository.countByReviewStatusNotIn(
                List.of(ReviewStatus.APPROVED, ReviewStatus.REJECTED)
        );

        long totalApproved = campaignRepository.countByApprovalStatus(ApprovalStatus.APPROVED);
        long achieved = totalApproved > 0
                ? campaignRepository.countAchievedCampaigns(ApprovalStatus.APPROVED)
                : 0;
        double achievedRatio = totalApproved > 0
                ? Math.round((double) achieved / totalApproved * 1000.0) / 10.0
                : 0.0;

        long totalUserCount = userRepository.count();

        return DashboardSummaryDTO.builder()
                .todayDonationAmount(todayDonation != null ? todayDonation : BigDecimal.ZERO)
                .activeCampaignCount(activeCampaignCount)
                .pendingFoundationCount(pendingFoundationCount)
                .achievedCampaignRatio(achievedRatio)
                .totalUserCount(totalUserCount)
                .totalDonationAmount(totalDonation != null ? totalDonation : BigDecimal.ZERO)
                .endedCampaignCount(endedCampaignCount)
                .settledCampaignCount(settledCampaignCount)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<DonationTrendDTO> getDonationTrend(int days) {
        LocalDateTime since = LocalDate.now().minusDays(days - 1).atStartOfDay();
        List<Object[]> rows = paymentRepository.findDailyDonationTrend(since);

        return rows.stream()
                .map(row -> DonationTrendDTO.builder()
                        .date((String) row[0])
                        .amount(new BigDecimal(row[1].toString()))
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryRatioDTO> getCategoryRatio() {
        // 카테고리별 캠페인 수
        List<Object[]> campaignRows = campaignRepository.countCampaignByCategory(ApprovalStatus.APPROVED);
        Map<String, Long> campaignCountMap = new HashMap<>();
        for (Object[] row : campaignRows) {
            String category = row[0].toString();
            Long count = ((Number) row[1]).longValue();
            campaignCountMap.put(category, count);
        }

        // 카테고리별 기부금 합계
        List<Object[]> donationRows = paymentRepository.findDonationAmountByCategory();
        Map<String, BigDecimal> donationAmountMap = new HashMap<>();
        for (Object[] row : donationRows) {
            String category = row[0].toString();
            BigDecimal amount = new BigDecimal(row[1].toString());
            donationAmountMap.put(category, amount);
        }

        // 전체 카테고리 순서대로 결합
        return Arrays.stream(CampaignCategory.values())
                .map(cat -> CategoryRatioDTO.builder()
                        .category(cat.name())
                        .categoryLabel(cat.getLabel())
                        .campaignCount(campaignCountMap.getOrDefault(cat.name(), 0L))
                        .donationAmount(donationAmountMap.getOrDefault(cat.name(), BigDecimal.ZERO))
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserRegistrationTrendDTO> getUserRegistrationTrend(int days) {
        LocalDateTime since = LocalDate.now().minusDays(days - 1).atStartOfDay();
        List<Object[]> rows = userRepository.findDailyUserRegistrations(since);

        return rows.stream()
                .map(row -> UserRegistrationTrendDTO.builder()
                        .date((String) row[0])
                        .count(((Number) row[1]).longValue())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AdminLogResponseDTO> getRecentLogs(Pageable pageable) {
        return adminLogRepository.findAll(pageable)
                .map(AdminLogResponseDTO::from);
    }
}

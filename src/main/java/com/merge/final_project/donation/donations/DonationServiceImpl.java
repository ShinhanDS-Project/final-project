package com.merge.final_project.donation.donations;

import com.merge.final_project.campaign.campaigns.ApprovalStatus;
import com.merge.final_project.campaign.campaigns.CampaignStatus;
import com.merge.final_project.campaign.campaigns.entity.Campaign;
import com.merge.final_project.campaign.campaigns.repository.CampaignRepository;
import com.merge.final_project.donation.donations.dto.*;
import com.merge.final_project.global.Image;
import com.merge.final_project.global.ImageRepository;
import com.merge.final_project.global.exceptions.BusinessException;
import com.merge.final_project.global.exceptions.ErrorCode;
import com.merge.final_project.user.users.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class DonationServiceImpl implements DonationService {


    private final DonationRepository donationRepository;
    private final UserRepository userRepository;
    private final CampaignRepository campaignRepository;

    private static final String CAMPAIGN_IMAGE_TARGET_NAME = "campaign";
    private static final String REPRESENTATIVE_IMAGE_PURPOSE = "REPRESENTATIVE";

    private static final List<CampaignStatus> HOME_VISIBLE_STATUSES = List.of(
            CampaignStatus.APPROVED,
            CampaignStatus.RECRUITING,
            CampaignStatus.ACTIVE
    );

    private final ImageRepository imageRepository;

    @Override
    public List<Donation> requestDonation(Long userNo) {
        //userNo 기준으로 기부내역 조회

        List<Donation> donation= donationRepository.findByUserNo(userNo);
        if(donation.isEmpty()){
            throw new BusinessException(ErrorCode.DONATION_INVALID);
        }

        return donation;
    }

    //[바다] main
    @Override
    public PublicStatsResponseDTO getPublicStats() {
        return PublicStatsResponseDTO.builder()
                .totalDonationCount(donationRepository.count())
                .totalUserCount(userRepository.count())
                .totalCampaignCount(campaignRepository.count())
                .build();
    }
    //main 캠페인 로드
    @Override
    public HomeHubResponseDTO getHomeHub() {
        var endingSoonPage = campaignRepository
                .findByCampaignStatusInAndApprovalStatusOrderByEndAtAscCampaignNoDesc(
                        HOME_VISIBLE_STATUSES,
                        ApprovalStatus.APPROVED,
                        PageRequest.of(0, 2)
                );

        var topParticipationPage = campaignRepository
                .findByCampaignStatusInAndApprovalStatusOrderByCurrentAmountDescCampaignNoDesc(
                        HOME_VISIBLE_STATUSES,
                        ApprovalStatus.APPROVED,
                        PageRequest.of(0, 3)
                );

        List<Campaign> merged = new ArrayList<>();
        merged.addAll(endingSoonPage.getContent());
        merged.addAll(topParticipationPage.getContent());

        Map<Long, String> imagePathByCampaignNo = buildRepresentativeImageMap(merged);

        return HomeHubResponseDTO.builder()
                .totalDonationCount(donationRepository.count())
                .totalUserCount(userRepository.count())
                .totalCampaignCount(campaignRepository.count())
                .endingSoon(toHomeItems(endingSoonPage.getContent(), imagePathByCampaignNo))
                .topParticipation(toHomeItems(topParticipationPage.getContent(), imagePathByCampaignNo))
                .build();
    }

    @Override
    public List<RecentDonationFeedItemDTO> getRecentPublicDonations(int limit) {
        int safeLimit = Math.min(20, Math.max(1, limit));

        List<Donation> donations = donationRepository.findTop50ByOrderByDonatedAtDesc()
                .stream()
                .limit(safeLimit)
                .toList();

        if (donations.isEmpty()) {
            return List.of();
        }

        List<Long> userNos = donations.stream()
                .filter(donation -> !donation.isAnonymous())
                .map(Donation::getUserNo)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        Map<Long, String> userNameByUserNo = userRepository.findAllById(userNos).stream()
                .collect(HashMap::new, (map, user) -> map.put(user.getUserNo(), user.getName()), HashMap::putAll);

        List<Long> campaignNos = donations.stream()
                .map(Donation::getCampaignNo)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        Map<Long, String> campaignTitleByNo = campaignRepository.findAllByCampaignNoIn(campaignNos).stream()
                .collect(HashMap::new, (map, campaign) -> map.put(campaign.getCampaignNo(), campaign.getTitle()), HashMap::putAll);

        return donations.stream()
                .map(donation -> RecentDonationFeedItemDTO.builder()
                        .name(resolveDisplayName(donation, userNameByUserNo))
                        .amount(donation.getDonationAmount() == null ? 0L : donation.getDonationAmount().longValue())
                        .donatedAt(donation.getDonatedAt())
                        .campaignTitle(campaignTitleByNo.getOrDefault(donation.getCampaignNo(), "캠페인"))
                        .build())
                .toList();
    }

    private String resolveDisplayName(Donation donation, Map<Long, String> userNameByUserNo) {
        if (donation.isAnonymous()) {
            return "익명";
        }

        String name = userNameByUserNo.get(donation.getUserNo());
        return maskName(name);
    }

    private String maskName(String name) {
        if (name == null || name.isBlank()) {
            return "기부자";
        }

        if (name.length() == 1) {
            return name + "*";
        }

        return name.substring(0, 1) + "*".repeat(Math.max(1, name.length() - 1));
    }

    private List<HomeCampaignItemDTO> toHomeItems(List<Campaign> campaigns, Map<Long, String> imagePathByCampaignNo) {
        return campaigns.stream().map(c -> {
            BigDecimal current = c.getCurrentAmount() == null ? BigDecimal.ZERO : c.getCurrentAmount();
            long currentAmount = current.longValue();
            long targetAmount = c.getTargetAmount() == null ? 0L : c.getTargetAmount();

            return HomeCampaignItemDTO.builder()
                    .campaignNo(c.getCampaignNo())
                    .imagePath(imagePathByCampaignNo.getOrDefault(c.getCampaignNo(), c.getImagePath()))
                    .title(c.getTitle())
                    .foundationName(c.getFoundation() == null ? null : c.getFoundation().getFoundationName())
                    .targetAmount(targetAmount)
                    .currentAmount(currentAmount)
                    .category(c.getCategory() == null ? null : c.getCategory().getLabel())
                    .progressPercent(calculateProgressPercent(current, targetAmount))
                    .daysLeft(calculateDaysLeft(c.getEndAt()))
                    .endAt(c.getEndAt() == null ? null : c.getEndAt().toString())
                    .build();
        }).toList();
    }

    private Map<Long, String> buildRepresentativeImageMap(List<Campaign> campaigns) {
        List<Long> campaignNos = campaigns.stream()
                .map(Campaign::getCampaignNo)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        if (campaignNos.isEmpty()) return Collections.emptyMap();

        List<Image> images = imageRepository.findByTargetNameAndPurposeAndTargetNoInOrderByTargetNoAscCreatedAtDesc(
                CAMPAIGN_IMAGE_TARGET_NAME,
                REPRESENTATIVE_IMAGE_PURPOSE,
                campaignNos
        );

        Map<Long, String> map = new HashMap<>();
        for (Image image : images) {
            map.putIfAbsent(image.getTargetNo(), image.getImgPath());
        }
        return map;
    }

    private int calculateProgressPercent(BigDecimal currentAmount, long targetAmount) {
        if (targetAmount <= 0) return 0;
        return currentAmount.multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(targetAmount), RoundingMode.DOWN)
                .min(BigDecimal.valueOf(100))
                .intValue();
    }

    private long calculateDaysLeft(LocalDateTime endAt) {
        if (endAt == null) return 0L;
        LocalDateTime now = LocalDateTime.now();
        if (endAt.isBefore(now)) return 0L;
        return Math.max(0L, Duration.between(now, endAt).toDays());
    }


    //[바다] 메인 캠페인 리스트
    @Override
    @Transactional(readOnly = true)
    public List<HomeLatestCampaignResponseDTO> getLatestOngoingCampaigns(int limit) {
        int safeLimit = (limit <= 0) ? 5 : Math.min(limit, 10);

        List<Campaign> campaigns = campaignRepository.findLatestOngoingCampaigns(
                PageRequest.of(0, safeLimit)
        );

        return campaigns.stream()
                .map(this::toHomeLatestCampaignResponseDTO)
                .toList();
    }

    private HomeLatestCampaignResponseDTO toHomeLatestCampaignResponseDTO(Campaign campaign) {
        BigDecimal currentAmount = campaign.getCurrentAmount() == null
                ? BigDecimal.ZERO
                : campaign.getCurrentAmount();

        long targetAmount = campaign.getTargetAmount() == null ? 0L : campaign.getTargetAmount();

        int progressPercent = 0;

        if (targetAmount > 0) {
            BigDecimal percent = currentAmount
                    .multiply(BigDecimal.valueOf(100))
                    .divide(BigDecimal.valueOf(targetAmount), 0, RoundingMode.HALF_UP);

            progressPercent = Math.min(percent.intValue(), 100);
        }

        return HomeLatestCampaignResponseDTO.builder()
                .campaignNo(campaign.getCampaignNo())
                .title(campaign.getTitle())
                .imagePath(resolveRepresentativeImagePath(campaign))
                .foundationName(
                        campaign.getFoundation() != null ? campaign.getFoundation().getFoundationName() : ""
                )
                .category(campaign.getCategory() != null ? campaign.getCategory().name() : "기타")
                .currentAmount(currentAmount.longValue())
                .targetAmount(targetAmount)
                .progressPercent(progressPercent)
                .endAt(campaign.getEndAt() != null ? campaign.getEndAt().toString() : null)
                .build();
    }

    private String resolveRepresentativeImagePath(Campaign campaign) {
        return imageRepository.findByTargetNameAndTargetNo("campaign", campaign.getCampaignNo())
                .stream()
                .filter(image -> "REPRESENTATIVE".equals(image.getPurpose()))
                .sorted(Comparator.comparing(Image::getCreatedAt).reversed())
                .map(Image::getImgPath)
                .findFirst()
                .orElse(campaign.getImagePath() == null ? "" : campaign.getImagePath());
    }


}

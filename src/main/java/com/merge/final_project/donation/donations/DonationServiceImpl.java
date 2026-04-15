package com.merge.final_project.donation.donations;

import com.merge.final_project.campaign.campaigns.ApprovalStatus;
import com.merge.final_project.campaign.campaigns.CampaignStatus;
import com.merge.final_project.campaign.campaigns.entity.Campaign;
import com.merge.final_project.campaign.campaigns.repository.CampaignRepository;
import com.merge.final_project.donation.donations.dto.HomeCampaignItemDTO;
import com.merge.final_project.donation.donations.dto.HomeHubResponseDTO;
import com.merge.final_project.donation.donations.dto.PublicStatsResponseDTO;
import com.merge.final_project.global.Image;
import com.merge.final_project.global.ImageRepository;
import com.merge.final_project.global.exceptions.BusinessException;
import com.merge.final_project.global.exceptions.ErrorCode;
import com.merge.final_project.user.users.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

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




}

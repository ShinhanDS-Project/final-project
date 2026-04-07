package com.merge.final_project.campaign.campaigns.service;

import com.merge.final_project.campaign.campaigns.ApprovalStatus;
import com.merge.final_project.campaign.campaigns.CampaignStatus;
import com.merge.final_project.campaign.campaigns.dto.CampaignListResponseDTO;
import com.merge.final_project.campaign.campaigns.dto.CampaignRequestDTO;
import com.merge.final_project.campaign.campaigns.entity.Campaign;
import com.merge.final_project.campaign.campaigns.repository.CampaignRepository;
import com.merge.final_project.campaign.useplan.entity.UsePlan;
import com.merge.final_project.campaign.useplan.repository.UsePlanRepository;
import com.merge.final_project.global.Image;
import com.merge.final_project.global.ImageRepository;
import com.merge.final_project.global.utils.FileUtil;
import com.merge.final_project.org.foundation.Foundation;
import com.merge.final_project.org.foundation.FoundationRepository;
import com.merge.final_project.wallet.Wallet;
import com.merge.final_project.wallet.entity.WalletStatus;
import com.merge.final_project.wallet.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Log4j2
public class CampaignServiceImpl implements CampaignService {
    private static final String CAMPAIGN_IMAGE_TARGET_NAME = "campaign";
    private static final String REPRESENTATIVE_IMAGE_PURPOSE = "REPRESENTATIVE";
    private static final String DETAIL_IMAGE_PURPOSE = "DETAIL";

    private final FoundationRepository foundationRepository;
    private final WalletRepository walletRepository;
    private final CampaignRepository campaignRepository;
    private final UsePlanRepository usePlanRepository;
    private final ImageRepository imageRepository;
    private final FileUtil fileUtil;

    // Ï∫†Ìéò???±Î°ù ?†Ï≤≠
    @Override
    @Transactional
    public void registerCampaign(CampaignRequestDTO dto, MultipartFile imageFile, List<MultipartFile> detailImageFiles, Long foundationNo) {
        // Í∏∞Î? ?®Ï≤¥ Ï°¥Ïû¨ ?¨Î? ?ïÏù∏
        Foundation foundation = foundationRepository.findById(foundationNo)
                .orElseThrow(() -> new IllegalArgumentException("?®Ï≤¥ ?ïÎ≥¥ ?ÜÏùå"));

        // Í∏∞Î? ?®Ï≤¥??ÏßÄÍ∞?Ï§?INACTIVE ÏßÄÍ∞??òÎÇò ?†Îãπ
        var walletAddresses = Stream.of(
                foundation.getCampaignWallet1(),
                foundation.getCampaignWallet2(),
                foundation.getCampaignWallet3()
        ).filter(Objects::nonNull).toList();

        Wallet availableWallet = walletRepository
                .findFirstByWalletAddressInAndStatus(walletAddresses, WalletStatus.INACTIVE)
                .orElseThrow(() -> new IllegalStateException("Îπ?ÏßÄÍ∞??ÜÏùå"));

        // Ï∫†Ìéò???îÌã∞???ùÏÑ±
        Campaign campaign = dto.toEntity();
        campaign.setFoundationNo(foundationNo);
        campaign.setWalletNo(availableWallet.getWalletNo().longValue());
        campaign.setCurrentAmount(0L);
        campaign.setApprovalStatus(ApprovalStatus.PENDING); // ?πÏù∏ ?ÄÍ∏?        campaign.setCampaignStatus(CampaignStatus.PENDING); // ÏßÑÌñâ ?ÄÍ∏?        campaign.setImagePath(null);

        Campaign savedCampaign = campaignRepository.save(campaign);

        // ?¥Î?ÏßÄ ?Ä??(?Ä???¥Î?ÏßÄ 1??+ ?ÅÏÑ∏ ?¥Î?ÏßÄ Î¶¨Ïä§??
        saveCampaignImage(imageFile, savedCampaign.getCampaignNo(), REPRESENTATIVE_IMAGE_PURPOSE);
        saveDetailImages(detailImageFiles, savedCampaign.getCampaignNo());

        // ?¨Ïö© Í≥ÑÌöç ?Ä??        if (dto.getUsePlans() != null) {
            dto.getUsePlans().forEach(planDto -> {
                UsePlan plan = planDto.toEntity(savedCampaign.getCampaignNo());
                usePlanRepository.save(plan);
            });
        }

        // ?¨Ïö©??ÏßÄÍ∞??ÅÌÉú ACTIVEÎ°?Î≥ÄÍ≤?        availableWallet.setStatus(WalletStatus.ACTIVE);
        walletRepository.save(availableWallet);
    }

    // Ï∫†Ìéò??Î™©Î°ù Ï°∞Ìöå
    @Override
    @Transactional(readOnly = true)
    public List<CampaignListResponseDTO> getCampaignList(String sort) {
        List<Campaign> campaigns;

        // ?ïÎ†¨ Ï°∞Í±¥ Î∂ÑÍ∏∞ : Î™®Í∏à?°Ïàú / ÎßàÍ∞ê?ÑÎ∞ï??        if ("participation".equalsIgnoreCase(sort)) {
            campaigns = campaignRepository.findByApprovalStatusOrderByCurrentAmountDescCampaignNoDesc(ApprovalStatus.APPROVED);
        } else {
            campaigns = campaignRepository.findByApprovalStatusOrderByEndAtAscCampaignNoDesc(ApprovalStatus.APPROVED);
        }

        return toCampaignListResponse(campaigns);
    }

    // ?îÌã∞??Î¶¨Ïä§??-> DTO Î¶¨Ïä§??Î≥Ä??    private List<CampaignListResponseDTO> toCampaignListResponse(List<Campaign> campaigns) {
        if (campaigns.isEmpty()) {
            return List.of();
        }

        // Ï∫†Ìéò???Ä???¥Î?ÏßÄ Í≤ΩÎ°úÎ•?Í∞Ä?∏Ï? Map???Ä??        Map<Long, String> imagePathByCampaignNo = imageRepository.findByTargetNameAndPurposeAndTargetNoIn(
                        CAMPAIGN_IMAGE_TARGET_NAME,
                        REPRESENTATIVE_IMAGE_PURPOSE,
                        campaigns.stream().map(Campaign::getCampaignNo).toList()
                ).stream()
                .sorted(Comparator.comparing(Image::getCreatedAt).reversed())
                .collect(Collectors.toMap(
                        Image::getTargetNo,
                        Image::getImgPath,
                        (existing, ignored) -> existing // Ï§ëÎ≥µ ??ÏµúÏã† ?∞Ïù¥???†Ï?
                ));

        // ÏµúÏ¢Ö DTO Ï°∞Î¶Ω
        return campaigns.stream()
                .map(campaign -> CampaignListResponseDTO.builder()
                        .campaignNo(campaign.getCampaignNo())
                        .imagePath(imagePathByCampaignNo.get(campaign.getCampaignNo()))
                        .title(campaign.getTitle())
                        // ?îÌã∞???∞Í?Í¥ÄÍ≥?@ManyToOne)Î•??¨Ïö©?òÏó¨ ?¨Îã® Î™ÖÏπ≠ ?çÎìù
                        .foundationName(campaign.getFoundation() != null ? campaign.getFoundation().getFoundationName() : null)
                        .targetAmount(campaign.getTargetAmount())
                        .currentAmount(campaign.getCurrentAmount())
                        .category(campaign.getCategory())
                        .endAt(campaign.getEndAt())
                        .build())
                .toList();
    }

    // ?ÅÏÑ∏ ?¥Î?ÏßÄ Î¶¨Ïä§???Ä??    private void saveDetailImages(List<MultipartFile> detailImageFiles, Long campaignNo) {
        if (detailImageFiles == null || detailImageFiles.isEmpty()) {
            return;
        }

        for (MultipartFile detailImageFile : detailImageFiles) {
            saveCampaignImage(detailImageFile, campaignNo, DETAIL_IMAGE_PURPOSE);
        }
    }

    // ?¥Î?ÏßÄ ?åÏù¥Î∏??àÏΩî???ùÏÑ± Î∞?Î¨ºÎ¶¨ ?åÏùº ?Ä???∏Ï∂ú
    private void saveCampaignImage(MultipartFile imageFile, Long campaignNo, String purpose) {
        if (imageFile == null || imageFile.isEmpty()) {
            return;
        }

        try {
            // ?åÏùº ?úÏä§?úÏóê Î¨ºÎ¶¨???åÏùº ?Ä??Î∞??Ä?•Îêú ?åÏùºÎ™?Î∞òÌôò
            String storedName = fileUtil.saveFile(imageFile);

            // DB???¥Î?ÏßÄ ?Ä??            imageRepository.save(Image.builder()
                    .imgPath("C:/uploads/" + storedName)
                    .imgOrgName(imageFile.getOriginalFilename())
                    .imgStoredName(storedName)
                    .targetName(CAMPAIGN_IMAGE_TARGET_NAME)
                    .targetNo(campaignNo)
                    .createdAt(LocalDateTime.now())
                    .purpose(purpose)
                    .build());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

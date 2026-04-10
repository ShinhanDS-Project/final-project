package com.merge.final_project.campaign.campaigns.service;

import com.merge.final_project.admin.adminlog.TargetType;
import com.merge.final_project.admin.sse.ApprovalRequestEvent;
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
import com.merge.final_project.org.Foundation;
import com.merge.final_project.org.FoundationRepository;
import com.merge.final_project.wallet.entity.Wallet;
import com.merge.final_project.wallet.repository.WalletRepository;
import com.merge.final_project.wallet.entity.WalletStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.ApplicationEventPublisher;
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
    private final ApplicationEventPublisher eventPublisher; // [가빈] SSE 이벤트 발행용

    @Override
    @Transactional
    public void registerCampaign(CampaignRequestDTO dto, MultipartFile imageFile, List<MultipartFile> detailImageFiles, Long foundationNo) {
        Foundation foundation = foundationRepository.findById(foundationNo)
            .orElseThrow(() -> new IllegalArgumentException("?? ??? ?? ? ????."));

        List<String> walletAddresses = Stream.of(
            foundation.getCampaignWallet1(),
            foundation.getCampaignWallet2(),
            foundation.getCampaignWallet3()
        ).filter(Objects::nonNull).toList();

        Wallet availableWallet = walletRepository
            .findFirstByWalletAddressInAndStatus(walletAddresses, WalletStatus.INACTIVE)
            .orElseThrow(() -> new IllegalStateException("?? ??? ??? ????."));

        Campaign campaign = dto.toEntity();
        campaign.setFoundationNo(foundationNo);
        campaign.setWalletNo(availableWallet.getWalletNo());
        campaign.setCurrentAmount(0L);
        campaign.setApprovalStatus(ApprovalStatus.PENDING);
        campaign.setCampaignStatus(CampaignStatus.PENDING);
        campaign.setUpdatedAt(LocalDateTime.now());

        Campaign savedCampaign = campaignRepository.save(campaign);

        saveCampaignImage(imageFile, savedCampaign.getCampaignNo(), REPRESENTATIVE_IMAGE_PURPOSE);
        saveDetailImages(detailImageFiles, savedCampaign.getCampaignNo());

        if (dto.getUsePlans() != null) {
            dto.getUsePlans().forEach(planDto -> {
                UsePlan plan = planDto.toEntity(savedCampaign.getCampaignNo());
                usePlanRepository.save(plan);
            });
        }

        availableWallet.changeStatus(WalletStatus.ACTIVE);
        walletRepository.save(availableWallet);

        // [가빈] 관리자에게 캠페인 승인 요청 SSE 알림
        eventPublisher.publishEvent(new ApprovalRequestEvent(
                TargetType.CAMPAIGN,
                savedCampaign.getCampaignNo(),
                savedCampaign.getTitle() + " 캠페인 승인 요청"));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CampaignListResponseDTO> getCampaignList(String sort) {
        List<Campaign> campaigns;

        if ("participation".equalsIgnoreCase(sort)) {
            campaigns = campaignRepository.findByCampaignStatusOrderByCurrentAmountDescCampaignNoDesc(CampaignStatus.RECRUITING);
        } else {
            campaigns = campaignRepository.findByCampaignStatusOrderByEndAtAscCampaignNoDesc(CampaignStatus.RECRUITING);
        }

        return toCampaignListResponse(campaigns);
    }

    private List<CampaignListResponseDTO> toCampaignListResponse(List<Campaign> campaigns) {
        if (campaigns.isEmpty()) {
            return List.of();
        }

        Map<Long, String> imagePathByCampaignNo = campaigns.stream()
            .collect(Collectors.toMap(
                Campaign::getCampaignNo,
                campaign -> imageRepository.findByTargetNameAndTargetNo(
                        CAMPAIGN_IMAGE_TARGET_NAME,
                        campaign.getCampaignNo()
                    ).stream()
                    .filter(image -> REPRESENTATIVE_IMAGE_PURPOSE.equals(image.getPurpose()))
                    .sorted(Comparator.comparing(Image::getCreatedAt).reversed())
                    .map(Image::getImgPath)
                    .findFirst()
                    .orElse(null)
            ));

        return campaigns.stream()
            .map(campaign -> CampaignListResponseDTO.builder()
                .campaignNo(campaign.getCampaignNo())
                .imagePath(imagePathByCampaignNo.get(campaign.getCampaignNo()))
                .title(campaign.getTitle())
                .foundationName(campaign.getFoundation() != null ? campaign.getFoundation().getFoundationName() : null)
                .targetAmount(campaign.getTargetAmount())
                .currentAmount(campaign.getCurrentAmount())
                .category(campaign.getCategory())
                .endAt(campaign.getEndAt())
                .build())
            .toList();
    }

    private void saveDetailImages(List<MultipartFile> detailImageFiles, Long campaignNo) {
        if (detailImageFiles == null || detailImageFiles.isEmpty()) {
            return;
        }

        for (MultipartFile detailImageFile : detailImageFiles) {
            saveCampaignImage(detailImageFile, campaignNo, DETAIL_IMAGE_PURPOSE);
        }
    }

    private void saveCampaignImage(MultipartFile imageFile, Long campaignNo, String purpose) {
        if (imageFile == null || imageFile.isEmpty()) {
            return;
        }

        try {
            String storedName = fileUtil.saveFile(imageFile);
            String filePath = fileUtil.getFilePath(storedName);

            imageRepository.save(Image.builder()
                .imgPath(filePath)
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

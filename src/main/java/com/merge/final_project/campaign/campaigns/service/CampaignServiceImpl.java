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
import com.merge.final_project.wallet.entity.Wallet;
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

    // 캠페인 등록 요청
    @Override
    @Transactional
    public void registerCampaign(CampaignRequestDTO dto, MultipartFile imageFile, List<MultipartFile> detailImageFiles, Long foundationNo) {
        // 기관 존재 여부 확인
        Foundation foundation = foundationRepository.findById(foundationNo)
                .orElseThrow(() -> new IllegalArgumentException("기관 정보가 없습니다."));

        // 기관이 가진 지갑 중 INACTIVE 상태 지갑 하나 할당
        var walletAddresses = Stream.of(
                foundation.getCampaignWallet1(),
                foundation.getCampaignWallet2(),
                foundation.getCampaignWallet3()
        ).filter(Objects::nonNull).toList();

        Wallet availableWallet = walletRepository
                .findFirstByWalletAddressInAndStatus(walletAddresses, WalletStatus.INACTIVE)
                .orElseThrow(() -> new IllegalStateException("사용 가능한 지갑이 없습니다."));

        // 캠페인 엔티티 생성
        Campaign campaign = dto.toEntity();
        campaign.setFoundationNo(foundationNo);
        campaign.setWalletNo(availableWallet.getWalletNo().longValue());
        campaign.setCurrentAmount(0L);
        campaign.setApprovalStatus(ApprovalStatus.PENDING);
        campaign.setCampaignStatus(CampaignStatus.PENDING);
        campaign.setImagePath(null);

        Campaign savedCampaign = campaignRepository.save(campaign);

        // 이미지 저장
        saveCampaignImage(imageFile, savedCampaign.getCampaignNo(), REPRESENTATIVE_IMAGE_PURPOSE);
        saveDetailImages(detailImageFiles, savedCampaign.getCampaignNo());

        // 사용 계획 저장
        if (dto.getUsePlans() != null) {
            dto.getUsePlans().forEach(planDTO -> {
                UsePlan plan = planDTO.toEntity(savedCampaign.getCampaignNo());
                usePlanRepository.save(plan);
            });
        }

        // 사용된 지갑 상태 변경
        availableWallet.setStatus(WalletStatus.ACTIVE);
        walletRepository.save(availableWallet);
    }

    // 캠페인 목록 조회
    @Override
    @Transactional(readOnly = true)
    public List<CampaignListResponseDTO> getCampaignList(String sort) {
        List<Campaign> campaigns;

        // 정렬 조건 분기: 참여율순 또는 마감임박순
        if ("participation".equalsIgnoreCase(sort)) {
            campaigns = campaignRepository.findByApprovalStatusOrderByCurrentAmountDescCampaignNoDesc(ApprovalStatus.APPROVED);
        } else {
            campaigns = campaignRepository.findByApprovalStatusOrderByEndAtAscCampaignNoDesc(ApprovalStatus.APPROVED);
        }

        return toCampaignListResponse(campaigns);
    }

    // 엔티티 목록을 DTO 목록으로 변환
    private List<CampaignListResponseDTO> toCampaignListResponse(List<Campaign> campaigns) {
        if (campaigns.isEmpty()) {
            return List.of();
        }

        // 캠페인별 대표 이미지 경로를 Map으로 구성
        Map<Long, String> imagePathByCampaignNo = imageRepository.findByTargetNameAndPurposeAndTargetNoIn(
                        CAMPAIGN_IMAGE_TARGET_NAME,
                        REPRESENTATIVE_IMAGE_PURPOSE,
                        campaigns.stream().map(Campaign::getCampaignNo).toList()
                ).stream()
                .sorted(Comparator.comparing(Image::getCreatedAt).reversed())
                .collect(Collectors.toMap(
                        Image::getTargetNo,
                        Image::getImgPath,
                        (existing, ignored) -> existing
                ));

        // 최종 DTO 조립
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

    // 상세 이미지 저장
    private void saveDetailImages(List<MultipartFile> detailImageFiles, Long campaignNo) {
        if (detailImageFiles == null || detailImageFiles.isEmpty()) {
            return;
        }

        for (MultipartFile detailImageFile : detailImageFiles) {
            saveCampaignImage(detailImageFile, campaignNo, DETAIL_IMAGE_PURPOSE);
        }
    }

    // 이미지 메타데이터와 실제 파일 저장
    private void saveCampaignImage(MultipartFile imageFile, Long campaignNo, String purpose) {
        if (imageFile == null || imageFile.isEmpty()) {
            return;
        }

        try {
            // 파일 시스템에 실제 파일 저장
            String storedName = fileUtil.saveFile(imageFile);

            // DB에 이미지 메타데이터 저장
            imageRepository.save(Image.builder()
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

package com.merge.final_project.campaign.campaigns.service;

import com.merge.final_project.campaign.campaigns.ApprovalStatus;
import com.merge.final_project.campaign.campaigns.CampaignStatus;
import com.merge.final_project.campaign.campaigns.dto.CampaignRequestDto;
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
import com.merge.final_project.wallet.WalletRepository;
import com.merge.final_project.wallet.entity.WalletStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Log4j2
public class CampaignServiceImpl implements CampaignService {
    private static final String CAMPAIGN_IMAGE_TARGET_NAME = "campaign";
    // 대표 이미지 구분
    private static final String REPRESENTATIVE_IMAGE_PURPOSE = "REPRESENTATIVE";
    // 상세 이미지 구분
    private static final String DETAIL_IMAGE_PURPOSE = "DETAIL";

    private final FoundationRepository foundationRepository;
    private final WalletRepository walletRepository;
    private final CampaignRepository campaignRepository;
    private final UsePlanRepository usePlanRepository;
    private final ImageRepository imageRepository;
    private final FileUtil fileUtil;

    // 신규 캠페인 등록
    @Override
    @Transactional
    public void registerCampaign(CampaignRequestDto dto, MultipartFile imageFile, List<MultipartFile> detailImageFiles, Long foundationNo) {
        // 기부단체 정보 확인
        Foundation foundation = foundationRepository.findById(foundationNo)
                .orElseThrow(() -> new IllegalArgumentException("기부 단체 정보를 찾을 수 없습니다."));

        // 기부단체 유효 지갑(INACTIVE) 필터링
        var walletAddresses = Stream.of(
                foundation.getCampaignWallet1(),
                foundation.getCampaignWallet2(),
                foundation.getCampaignWallet3()
        ).filter(Objects::nonNull).toList();

        // INACTIVE 지갑 하나 조회
        Wallet availableWallet = walletRepository
                .findFirstByWalletAddressInAndStatus(walletAddresses, WalletStatus.INACTIVE)
                .orElseThrow(() -> new IllegalStateException("모든 지갑이 사용중 입니다."));

        // 캠페인 엔티티 생성
        Campaign campaign = dto.toEntity();
        campaign.setFoundationNo(foundationNo);
        campaign.setWalletNo(availableWallet.getWalletNo().longValue());
        campaign.setCurrentAmount(0L);
        campaign.setApprovalStatus(ApprovalStatus.PENDING); // 승인 대기 상태
        campaign.setCampaignStatus(CampaignStatus.PENDING); // 캠페인 대기 상태
        campaign.setImagePath(null);

        // 캠페인 기본 정보 DB 저장
        Campaign savedCampaign = campaignRepository.save(campaign);

        // (대표 이미지) image 테이블에 저장
        saveCampaignImage(imageFile, savedCampaign.getCampaignNo(), REPRESENTATIVE_IMAGE_PURPOSE);

        // (상세 이미지) image 테이블에 저장(반복문 활용)
        saveDetailImages(detailImageFiles, savedCampaign.getCampaignNo());

        // 지출 계획 리스트 저장
        if (dto.getUsePlans() != null) {
            dto.getUsePlans().forEach(planDto -> {
                UsePlan plan = planDto.toEntity(savedCampaign.getCampaignNo());
                usePlanRepository.save(plan);
            });
        }

        // 캠페인 등록 시 INACTIVE 지갑의 상태 ACTIVE로 변경
        availableWallet.setStatus(WalletStatus.ACTIVE);
        walletRepository.save(availableWallet);
    }

    // 상세 이미지 리스트 각각 저장(1개 당 1개 row 로 저장)
    private void saveDetailImages(List<MultipartFile> detailImageFiles, Long campaignNo) {
        if (detailImageFiles == null || detailImageFiles.isEmpty()) {
            return;
        }

        for (MultipartFile detailImageFile : detailImageFiles) {
            saveCampaignImage(detailImageFile, campaignNo, DETAIL_IMAGE_PURPOSE);
        }
    }

    // utils 패키지 FileUtil.java 사용 (대표/상세 이미지 저장)
    private void saveCampaignImage(MultipartFile imageFile, Long campaignNo, String purpose) {
        if (imageFile == null || imageFile.isEmpty()) {
            return;
        }

        try {
            String storedName = fileUtil.saveFile(imageFile);

            // image 엔티티 사용해 DB에 저장
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
            throw new RuntimeException("이미지 저장 실패",e);
        }
    }
}

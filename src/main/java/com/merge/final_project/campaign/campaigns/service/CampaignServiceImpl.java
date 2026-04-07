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
                .orElseThrow(() -> new IllegalArgumentException("Foundation not found."));

        // 재단 보유 지갑 주소들 중 유효한 것 필터링
        var walletAddresses = Stream.of(
                foundation.getCampaignWallet1(),
                foundation.getCampaignWallet2(),
                foundation.getCampaignWallet3()
        ).filter(Objects::nonNull).toList();

        // INACTIVE 상태인 지갑 하나 조회
        Wallet availableWallet = walletRepository
                .findFirstByWalletAddressInAndStatus(walletAddresses, WalletStatus.INACTIVE)
                .orElseThrow(() -> new IllegalStateException("No inactive wallet available."));

        // 캠페인 엔티티 생성
        Campaign campaign = dto.toEntity();
        campaign.setFoundationNo(foundationNo);
        campaign.setWalletNo(availableWallet.getWalletNo().longValue());
        campaign.setCurrentAmount(0L);
        campaign.setApprovalStatus(ApprovalStatus.PENDING);
        campaign.setCampaignStatus(CampaignStatus.PENDING);
        campaign.setImagePath(saveRepresentativeImage(imageFile));

        // 캠페인 정보 DB 저장
        Campaign savedCampaign = campaignRepository.save(campaign);

        // 이미지 파일 있을 경우 저장
        saveDetailImages(detailImageFiles, savedCampaign.getCampaignNo());

        // 지출 계획 리스트 저장
        if (dto.getUsePlans() != null) {
            dto.getUsePlans().forEach(planDto -> {
                UsePlan plan = planDto.toEntity(savedCampaign.getCampaignNo());
                usePlanRepository.save(plan);
            });
        }

        // 사용된 지갑의 상태를 ACTIVE로 변경
        availableWallet.setStatus(WalletStatus.ACTIVE);
        walletRepository.save(availableWallet);
    }

    private String saveRepresentativeImage(MultipartFile imageFile) {
        if (imageFile == null || imageFile.isEmpty()) {
            return null;
        }

        try {
            String storedName = fileUtil.saveFile(imageFile);
            return "C:/uploads/" + storedName;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void saveDetailImages(List<MultipartFile> detailImageFiles, Long campaignNo) {
        if (detailImageFiles == null || detailImageFiles.isEmpty()) {
            return;
        }

        for (MultipartFile detailImageFile : detailImageFiles) {
            if (detailImageFile == null || detailImageFile.isEmpty()) {
                continue;
            }

            try {
                String storedName = fileUtil.saveFile(detailImageFile);
                imageRepository.save(Image.builder()
                        .imgPath("C:/uploads/" + storedName)
                        .imgOrgName(detailImageFile.getOriginalFilename())
                        .imgStoredName(storedName)
                        .targetName("campaign")
                        .targetNo(campaignNo)
                        .createdAt(LocalDateTime.now())
                        .build());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}

package com.merge.final_project.campaign.campaigns.service;

import com.merge.final_project.campaign.campaigns.ApprovalStatus;
import com.merge.final_project.campaign.campaigns.CampaignStatus;
import com.merge.final_project.campaign.campaigns.dto.CampaignRequestDTO;
import com.merge.final_project.campaign.campaigns.entity.Campaign;
import com.merge.final_project.campaign.campaigns.repository.CampaignRepository;
import com.merge.final_project.campaign.useplan.dto.UsePlanRequestDTO;
import com.merge.final_project.campaign.useplan.entity.UsePlan;
import com.merge.final_project.campaign.useplan.repository.UsePlanRepository;
import com.merge.final_project.global.Image;
import com.merge.final_project.global.ImageRepository;
import com.merge.final_project.wallet.entity.Wallet;
import com.merge.final_project.wallet.repository.WalletRepository;
import com.merge.final_project.wallet.entity.WalletStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class CampaignServiceImplTest {

    @Autowired
    private CampaignService campaignService;

    @Autowired
    private CampaignRepository campaignRepository;

    @Autowired
    private UsePlanRepository usePlanRepository;

    @Autowired
    private ImageRepository imageRepository;

    @Autowired
    private WalletRepository walletRepository;

    @Test
    @Transactional
    @Rollback(false)
    @DisplayName("캠페인 등록 시 엔티티, 지출계획, 이미지, 지갑 상태가 정상적으로 저장된다")
    void registerCampaign_success() {
        Long foundationId = 888L;

        String campaignTitle = "통합 테스트용 캠페인-" + System.currentTimeMillis();

        List<String> myWallets = List.of(
            "0x888_BUSY_ADDR",
            "0x888_FREE_ADDR_1",
            "0x888_FREE_ADDR_2"
        );

        // 테스트 전: 사용 가능한(INACTIVE) 지갑 개수 확인
        long beforeInactiveCount = myWallets.stream()
            .map(walletRepository::findByWalletAddress)
            .flatMap(Optional::stream)
            .filter(w -> w.getStatus() == WalletStatus.INACTIVE)
            .count();

        assertThat(beforeInactiveCount).isGreaterThan(0);

        // 캠페인 정보 설정
        CampaignRequestDTO dto = new CampaignRequestDTO();
        dto.setTitle(campaignTitle);
        dto.setDescription("길거리 동물들을 위한 사료 및 치료비 지원 프로젝트입니다.");
        dto.setCategory("동물");
        dto.setTargetAmount(2_000_000L);
        dto.setStartAt(LocalDateTime.of(2025, 1, 1, 0, 0));
        dto.setEndAt(LocalDateTime.of(2025, 3, 31, 23, 59, 59));
        dto.setUsageStartAt(LocalDateTime.of(2025, 4, 1, 0, 0));
        dto.setUsageEndAt(LocalDateTime.of(2025, 6, 30, 23, 59, 59));

        // 가짜 이미지 파일 생성
        MockMultipartFile imageFile = new MockMultipartFile(
            "imageFile",
            "images.png",
            "image/png",
            "test-image".getBytes()
        );

        MockMultipartFile detailImage1 = new MockMultipartFile(
            "detailImageFiles",
            "detail-1.png",
            "image/png",
            "detail-image-1".getBytes()
        );

        MockMultipartFile detailImage2 = new MockMultipartFile(
            "detailImageFiles",
            "detail-2.png",
            "image/png",
            "detail-image-2".getBytes()
        );

        // 지출 계획 설정
        UsePlanRequestDTO plan1 = new UsePlanRequestDTO();
        plan1.setPlanContent("안녕");
        plan1.setPlanAmount(1_500_000L);

        UsePlanRequestDTO plan2 = new UsePlanRequestDTO();
        plan2.setPlanContent("메롱");
        plan2.setPlanAmount(500_000L);

        dto.setUsePlans(List.of(plan1, plan2));

        // 서비스 호출 (실제 로직 실행)
        campaignService.registerCampaign(dto, imageFile, List.of(detailImage1, detailImage2), foundationId);

        // 저장된 캠페인 조회
        Campaign savedCampaign = campaignRepository.findAll().stream()
            .filter(c -> campaignTitle.equals(c.getTitle()))
            .filter(c -> foundationId.equals(c.getFoundationNo()))
            .max((left, right) -> Long.compare(left.getCampaignNo(), right.getCampaignNo()))
            .orElseThrow(() -> new IllegalArgumentException("캠페인이 저장되지 않았습니다."));

        // 캠페인 기본 정보 검증
        assertThat(savedCampaign.getTitle()).isEqualTo(campaignTitle);
        assertThat(savedCampaign.getImagePath()).isNull();
        assertThat(savedCampaign.getApprovalStatus()).isEqualTo(ApprovalStatus.PENDING);
        assertThat(savedCampaign.getCampaignStatus()).isEqualTo(CampaignStatus.PENDING);
        assertThat(savedCampaign.getCurrentAmount()).isEqualTo(0L);

        assertThat(savedCampaign.getStartAt()).isEqualTo(LocalDateTime.of(2025, 1, 1, 0, 0));
        assertThat(savedCampaign.getEndAt()).isEqualTo(LocalDateTime.of(2025, 3, 31, 23, 59, 59));
        assertThat(savedCampaign.getUsageStartAt()).isEqualTo(LocalDateTime.of(2025, 4, 1, 0, 0));
        assertThat(savedCampaign.getUsageEndAt()).isEqualTo(LocalDateTime.of(2025, 6, 30, 23, 59, 59));

        // 지출 계획 저장 여부 검증
        List<UsePlan> savedPlans = usePlanRepository.findAll().stream()
            .filter(plan -> savedCampaign.getCampaignNo().equals(plan.getCampaignNo()))
            .toList();

        assertThat(savedPlans).hasSize(2);
        assertThat(savedPlans).extracting("planContent")
            .containsExactlyInAnyOrder("안녕", "메롱");

        // 이미지 저장 여부 검증
        List<Image> savedImages = imageRepository.findByTargetNameAndTargetNo("campaign", savedCampaign.getCampaignNo());

        assertThat(savedImages).hasSize(3);
        assertThat(savedImages)
            .extracting(Image::getPurpose)
            .containsExactlyInAnyOrder("REPRESENTATIVE", "DETAIL", "DETAIL");

        Image representativeImage = savedImages.stream()
            .filter(image -> "REPRESENTATIVE".equals(image.getPurpose()))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("대표 이미지가 저장되지 않았습니다."));

        assertThat(representativeImage.getImgOrgName()).isEqualTo("images.png");
        assertThat(representativeImage.getImgStoredName()).contains("_images.png");
        assertThat(representativeImage.getImgPath()).contains(representativeImage.getImgStoredName());

        List<Image> detailImages = savedImages.stream()
            .filter(image -> "DETAIL".equals(image.getPurpose()))
            .sorted(Comparator.comparing(Image::getImgOrgName))
            .toList();

        assertThat(detailImages).hasSize(2);
        assertThat(detailImages)
            .extracting(Image::getImgOrgName)
            .containsExactly("detail-1.png", "detail-2.png");

        // 지갑 상태 변경 검증 (INACTIVE -> ACTIVE)
        Wallet usedWallet = walletRepository.findById(savedCampaign.getWalletNo())
            .orElseThrow(() -> new IllegalArgumentException("할당된 지갑을 찾을 수 없습니다."));

        assertThat(usedWallet.getStatus()).isEqualTo(WalletStatus.ACTIVE);

        // 테스트 후: 사용 가능한 지갑 개수가 1개 줄었는지 확인
        long afterInactiveCount = myWallets.stream()
            .map(walletRepository::findByWalletAddress)
            .flatMap(Optional::stream)
            .filter(w -> w.getStatus() == WalletStatus.INACTIVE)
            .count();

        assertThat(afterInactiveCount).isEqualTo(beforeInactiveCount - 1);
    }
}

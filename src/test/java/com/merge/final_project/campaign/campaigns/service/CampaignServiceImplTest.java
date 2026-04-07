package com.merge.final_project.campaign.campaigns.service;

import com.merge.final_project.campaign.campaigns.ApprovalStatus;
import com.merge.final_project.campaign.campaigns.dto.CampaignRequestDto;
import com.merge.final_project.campaign.campaigns.entity.Campaign;
import com.merge.final_project.campaign.campaigns.repository.CampaignRepository;
import com.merge.final_project.campaign.useplan.dto.UsePlanRequestDto;
import com.merge.final_project.campaign.useplan.entity.UsePlan;
import com.merge.final_project.campaign.useplan.repository.UsePlanRepository;
import com.merge.final_project.global.Image;
import com.merge.final_project.global.ImageRepository;
import com.merge.final_project.wallet.Wallet;
import com.merge.final_project.wallet.WalletRepository;
import com.merge.final_project.wallet.entity.WalletStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
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
    @Rollback(false)
    @DisplayName("캠페인 등록: 이미지 저장 + 지갑 상태 확인 및 변경 + 지출 내역 등록")
    void registerCampaign_success() {
        Long organizationId = 888L;

        // 테스트 실행 시마다 제목이 겹치지 않게 현재 시간 추가
        String campaignTitle = "캠페인-최종이다-" + System.currentTimeMillis();

        List<String> myWallets = List.of(
                "0x888_BUSY_ADDR",
                "0x888_FREE_ADDR_1",
                "0x888_FREE_ADDR_2"
        );

        // 등록 전 INACTIVE 상태의 지갑 개수 확인
        long beforeInactiveCount = walletRepository.findAll().stream()
                .filter(w -> myWallets.contains(w.getWalletAddress()) && w.getStatus() == WalletStatus.INACTIVE)
                .count();

        // INACTIVE 지갑이 미리 존재해야 테스트 가능
        assertThat(beforeInactiveCount).isGreaterThan(0);

        // 캠페인 등록 정보 입력
        CampaignRequestDto dto = new CampaignRequestDto();
        dto.setTitle(campaignTitle);
        dto.setDescription("이미지 저장 테스트");
        dto.setTargetAmount(2000000L);

        // 기간 설정
        dto.setStartAt(LocalDateTime.of(2025, 1, 1, 0, 0));
        dto.setEndAt(LocalDateTime.of(2025, 3, 31, 23, 59, 59));
        dto.setUsageStartAt(LocalDateTime.of(2025, 4, 1, 0, 0));
        dto.setUsageEndAt(LocalDateTime.of(2025, 6, 30, 23, 59, 59));

        // 대표 이미지 1장
        MockMultipartFile imageFile = new MockMultipartFile(
                "imageFile",
                "main_thumb.png",
                "image/png",
                "test-image".getBytes()
        );

        // 상세 이미지 여러 장
        MockMultipartFile detailImageFile1 = new MockMultipartFile(
                "detailImageFiles",
                "img1.jpg",
                "image/jpeg",
                "detail-image-1".getBytes()
        );

        MockMultipartFile detailImageFile2 = new MockMultipartFile(
                "detailImageFiles",
                "img2.jpg",
                "image/jpeg",
                "detail-image-2".getBytes()
        );

        MockMultipartFile detailImageFile3 = new MockMultipartFile(
                "detailImageFiles",
                "img3.jpg",
                "image/jpeg",
                "detail-image-3".getBytes()
        );

        // 지출 계획 데이터 구성
        UsePlanRequestDto plan1 = new UsePlanRequestDto();
        plan1.setPlanContent("너랑나");
        plan1.setPlanAmount(1500000L);

        UsePlanRequestDto plan2 = new UsePlanRequestDto();
        plan2.setPlanContent("잔소리");
        plan2.setPlanAmount(500000L);

        dto.setUsePlans(List.of(plan1, plan2));

        // 서비스 로직 실행
        campaignService.registerCampaign(dto, imageFile, List.of(detailImageFile1, detailImageFile2, detailImageFile3), organizationId);

        // 캠페인 본체 저장 확인
        Campaign savedCampaign = campaignRepository.findAll().stream()
                .filter(c -> campaignTitle.equals(c.getTitle()))
                .filter(c -> organizationId.equals(c.getFoundationNo()))
                .max((left, right) -> Long.compare(left.getCampaignNo(), right.getCampaignNo()))
                .orElseThrow(() -> new IllegalArgumentException("캠페인이 생성되지 않았습니다."));

        assertThat(savedCampaign.getTitle()).isEqualTo(campaignTitle);
        assertThat(savedCampaign.getImagePath()).isNull();
        assertThat(savedCampaign.getApprovalStatus()).isEqualTo(ApprovalStatus.PENDING);

        // 지출 계획 저장 확인
        List<UsePlan> savedPlans = usePlanRepository.findAll().stream()
                .filter(plan -> savedCampaign.getCampaignNo().equals(plan.getCampaignNo()))
                .toList();

        assertThat(savedPlans).hasSize(2);

        // image 테이블에 대표 1건 + 상세 3건이 각각 row로 저장되는지 확인
        List<Image> savedImages = imageRepository.findByTargetNameAndTargetNo("campaign", savedCampaign.getCampaignNo());

        assertThat(savedImages).hasSize(4);
        assertThat(savedImages).extracting(Image::getImgOrgName)
                .containsExactlyInAnyOrder("main_thumb.png", "img1.jpg", "img2.jpg", "img3.jpg");

        // 대표 이미지 purpose 확인
        assertThat(savedImages.stream()
                .filter(image -> "REPRESENTATIVE".equals(image.getPurpose()))
                .map(Image::getImgOrgName)
                .toList()).containsExactly("main_thumb.png");

        // 상세 이미지 purpose 확인
        assertThat(savedImages.stream()
                .filter(image -> "DETAIL".equals(image.getPurpose()))
                .map(Image::getImgOrgName)
                .toList()).containsExactlyInAnyOrder("img1.jpg", "img2.jpg", "img3.jpg");

        assertThat(savedImages).allSatisfy(image -> {
            assertThat(image.getImgStoredName()).isNotBlank();
            assertThat(image.getImgPath()).contains(image.getImgStoredName());
            assertThat(image.getPurpose()).isIn("REPRESENTATIVE", "DETAIL");
        });

        // 배정된 지갑 상태 확인
        Wallet usedWallet = walletRepository.findById(savedCampaign.getWalletNo())
                .orElseThrow(() -> new IllegalArgumentException("배정된 지갑을 찾을 수 없습니다."));

        assertThat(usedWallet.getStatus()).isEqualTo(WalletStatus.ACTIVE);

        // 비활성화 지갑 개수 감소 확인
        long afterInactiveCount = walletRepository.findAll().stream()
                .filter(w -> myWallets.contains(w.getWalletAddress()) && w.getStatus() == WalletStatus.INACTIVE)
                .count();

        assertThat(afterInactiveCount).isEqualTo(beforeInactiveCount - 1);
    }
}

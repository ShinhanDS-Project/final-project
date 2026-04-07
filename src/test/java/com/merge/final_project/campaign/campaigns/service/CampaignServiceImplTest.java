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
    @DisplayName("캠페인 등록 시 본체 저장, 지출 계획 수립, 승인 대기 상태 및 지갑 활성화 확인")
    void registerCampaign_success() {
        Long organizationId = 888L;

        // 테스트 실행 시마다 제목이 겹치지 않도록 현재 시간 추가
        String campaignTitle = "오늘은 정말 두개 완성해야지 사업-" + System.currentTimeMillis();

        List<String> myWallets = List.of(
                "0x888_BUSY_ADDR",
                "0x888_FREE_ADDR_1",
                "0x888_FREE_ADDR_2"
        );

        // 등록 전 INACTIVE 상태인 지갑 개수 확인
        long beforeInactiveCount = walletRepository.findAll().stream()
                .filter(w -> myWallets.contains(w.getWalletAddress()) && w.getStatus() == WalletStatus.INACTIVE)
                .count();

        // INACTIVE 지갑이 미리 존재해야 테스트 성공
        assertThat(beforeInactiveCount).isGreaterThan(0);

        // 캠페인 등록 정보 입력 (DTO)
        CampaignRequestDto dto = new CampaignRequestDto();
        dto.setTitle(campaignTitle);
        dto.setDescription("코딩 프로젝트입니다.");
        dto.setTargetAmount(2000000L);

        // 기간 설정
        dto.setStartAt(LocalDateTime.of(2025, 1, 1, 0, 0));
        dto.setEndAt(LocalDateTime.of(2025, 3, 31, 23, 59, 59));
        dto.setUsageStartAt(LocalDateTime.of(2025, 4, 1, 0, 0));
        dto.setUsageEndAt(LocalDateTime.of(2025, 6, 30, 23, 59, 59));

        // 테스트용 이미지 파일 생성 (대표 이미지 1장 + 상세 이미지 3장)
        MockMultipartFile imageFile = new MockMultipartFile(
                "imageFile",
                "main_thumb.png",
                "image/png",
                "test-image".getBytes()
        );

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
        plan1.setPlanContent("가빈이");
        plan1.setPlanAmount(1500000L);

        UsePlanRequestDto plan2 = new UsePlanRequestDto();
        plan2.setPlanContent("바다");
        plan2.setPlanAmount(500000L);

        dto.setUsePlans(List.of(plan1, plan2));

        // 상세 이미지는 리스트로 전달
        campaignService.registerCampaign(dto, imageFile, List.of(detailImageFile1, detailImageFile2, detailImageFile3), organizationId);

        // 캠페인 본체 저장 확인
        Campaign savedCampaign = campaignRepository.findAll().stream()
                .filter(c -> campaignTitle.equals(c.getTitle()))
                .filter(c -> organizationId.equals(c.getFoundationNo()))
                .max((left, right) -> Long.compare(left.getCampaignNo(), right.getCampaignNo()))
                .orElseThrow(() -> new IllegalArgumentException("캠페인이 생성되지 않았습니다."));

        assertThat(savedCampaign.getTitle()).isEqualTo(campaignTitle);
        // 대표 이미지 경로 확인
        assertThat(savedCampaign.getImagePath()).contains("main_thumb.png");
        // 승인 대기 상태 확인
        assertThat(savedCampaign.getApprovalStatus()).isEqualTo(ApprovalStatus.PENDING);

        // 기간 데이터 검증
        assertThat(savedCampaign.getStartAt()).isEqualTo(LocalDateTime.of(2025, 1, 1, 0, 0));
        assertThat(savedCampaign.getEndAt()).isEqualTo(LocalDateTime.of(2025, 3, 31, 23, 59, 59));

        // 지출 계획 저장 확인
        List<UsePlan> savedPlans = usePlanRepository.findAll().stream()
                .filter(plan -> savedCampaign.getCampaignNo().equals(plan.getCampaignNo()))
                .toList();

        assertThat(savedPlans).hasSize(2);
        assertThat(savedPlans).extracting("planContent")
                .containsExactlyInAnyOrder("가빈이", "바다");

        // 이미지 테이블 상세 이미지 저장 확인
        List<Image> savedImages = imageRepository.findByTargetNameAndTargetNo("campaign", savedCampaign.getCampaignNo());

        // 상세 이미지 3장 확인
        assertThat(savedImages).hasSize(3);
        assertThat(savedImages).extracting(Image::getImgOrgName)
                .containsExactlyInAnyOrder("img1.jpg", "img2.jpg", "img3.jpg");

        assertThat(savedImages).allSatisfy(image -> {
            assertThat(image.getImgStoredName()).isNotBlank();
            assertThat(image.getImgPath()).contains(image.getImgStoredName());
        });

        // 배정된 지갑 상태 확인 (INACTIVE -> ACTIVE)
        Wallet usedWallet = walletRepository.findById(savedCampaign.getWalletNo())
                .orElseThrow(() -> new IllegalArgumentException("배정된 지갑을 찾을 수 없습니다."));

        assertThat(usedWallet.getStatus()).isEqualTo(WalletStatus.ACTIVE);

        // 전체 비활성화 지갑 개수가 1개 줄었는지 확인
        long afterInactiveCount = walletRepository.findAll().stream()
                .filter(w -> myWallets.contains(w.getWalletAddress()) && w.getStatus() == WalletStatus.INACTIVE)
                .count();

        assertThat(afterInactiveCount).isEqualTo(beforeInactiveCount - 1);
    }
}
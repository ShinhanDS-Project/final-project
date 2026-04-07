package com.merge.final_project.campaign.campaigns.service;

import com.merge.final_project.campaign.campaigns.ApprovalStatus;
import com.merge.final_project.campaign.campaigns.dto.CampaignRequestDTO;
import com.merge.final_project.campaign.campaigns.entity.Campaign;
import com.merge.final_project.campaign.campaigns.repository.CampaignRepository;
import com.merge.final_project.campaign.useplan.dto.UsePlanRequestDTO;
import com.merge.final_project.campaign.useplan.entity.UsePlan;
import com.merge.final_project.campaign.useplan.repository.UsePlanRepository;
import com.merge.final_project.global.Image;
import com.merge.final_project.global.ImageRepository;
import com.merge.final_project.wallet.Wallet;
import com.merge.final_project.wallet.entity.WalletStatus;
import com.merge.final_project.wallet.repository.WalletRepository;
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
    @DisplayName("캠페인 등록 + 이미지, 사용계획, 지갑 상태 검증 테스트")
    void registerCampaign_success() {

        // 단체 ID
        Long organizationId = 888L;

        // 캠페인 제목
        String campaignTitle = "캠페인" + System.currentTimeMillis();

        // 사용할 지갑 주소 목록
        List<String> myWallets = List.of(
                "0x888_BUSY_ADDR",
                "0x888_FREE_ADDR_1",
                "0x888_FREE_ADDR_2"
        );

        // 테스트 전 INACTIVE 상태 지갑 개수 확인
        long beforeInactiveCount = walletRepository.findAll().stream()
                .filter(w -> myWallets.contains(w.getWalletAddress()) && w.getStatus() == WalletStatus.INACTIVE)
                .count();

        assertThat(beforeInactiveCount).isGreaterThan(0);

        // 캠페인 요청 DTO 설정
        CampaignRequestDTO dto = new CampaignRequestDTO();
        dto.setTitle(campaignTitle); // 캠페인 제목
        dto.setDescription("테스트 등록 테스트"); // 설명
        dto.setTargetAmount(2_000_000L); // 목표 금액
        dto.setStartAt(LocalDateTime.of(2025, 1, 1, 0, 0)); // 시작일
        dto.setEndAt(LocalDateTime.of(2025, 3, 31, 23, 59, 59)); // 종료일
        dto.setUsageStartAt(LocalDateTime.of(2025, 4, 1, 0, 0)); // 사용 시작일
        dto.setUsageEndAt(LocalDateTime.of(2025, 6, 30, 23, 59, 59)); // 사용 종료일

        // 대표 이미지
        MockMultipartFile imageFile = new MockMultipartFile(
                "imageFile",
                "대표이미지.png",
                "image/png",
                "테스트이미지".getBytes()
        );

        // 상세 이미지들
        MockMultipartFile detailImageFile1 = new MockMultipartFile(
                "detailImageFiles",
                "img1.jpg",
                "image/jpeg",
                "상세이미지1".getBytes()
        );

        MockMultipartFile detailImageFile2 = new MockMultipartFile(
                "detailImageFiles",
                "img2.jpg",
                "image/jpeg",
                "상세이미지2".getBytes()
        );

        MockMultipartFile detailImageFile3 = new MockMultipartFile(
                "detailImageFiles",
                "img3.jpg",
                "image/jpeg",
                "상세이미지3".getBytes()
        );

        // 사용 계획
        UsePlanRequestDTO plan1 = new UsePlanRequestDTO();
        plan1.setPlanContent("의료 지원");
        plan1.setPlanAmount(1_500_000L);

        UsePlanRequestDTO plan2 = new UsePlanRequestDTO();
        plan2.setPlanContent("물품 지원");
        plan2.setPlanAmount(500_000L);

        dto.setUsePlans(List.of(plan1, plan2));

        // 캠페인 등록 실행
        campaignService.registerCampaign(
                dto,
                imageFile,
                List.of(detailImageFile1, detailImageFile2, detailImageFile3),
                organizationId
        );

        // 저장된 캠페인 조회
        Campaign savedCampaign = campaignRepository.findAll().stream()
                .filter(c -> campaignTitle.equals(c.getTitle()))
                .filter(c -> organizationId.equals(c.getFoundationNo()))
                .max((left, right) -> Long.compare(left.getCampaignNo(), right.getCampaignNo()))
                .orElseThrow(() -> new IllegalArgumentException("캠페인이 생성되지 않았습니다."));

        // 기본 정보 검증
        assertThat(savedCampaign.getTitle()).isEqualTo(campaignTitle);
        assertThat(savedCampaign.getImagePath()).isNull();
        assertThat(savedCampaign.getApprovalStatus()).isEqualTo(ApprovalStatus.PENDING);

        // 사용 계획 검증
        List<UsePlan> savedPlans = usePlanRepository.findAll().stream()
                .filter(plan -> savedCampaign.getCampaignNo().equals(plan.getCampaignNo()))
                .toList();

        assertThat(savedPlans).hasSize(2);

        // 이미지 검증
        List<Image> savedImages =
                imageRepository.findByTargetNameAndTargetNo("campaign", savedCampaign.getCampaignNo());

        // 총 4개 (대표 1 + 상세 3)
        assertThat(savedImages).hasSize(4);

        assertThat(savedImages).extracting(Image::getImgOrgName)
                .containsExactlyInAnyOrder("대표이미지.png", "img1.jpg", "img2.jpg", "img3.jpg");

        // 대표 이미지 확인
        assertThat(savedImages.stream()
                .filter(image -> "REPRESENTATIVE".equals(image.getPurpose()))
                .map(Image::getImgOrgName)
                .toList()).containsExactly("대표이미지.png");

        // 상세 이미지 확인
        assertThat(savedImages.stream()
                .filter(image -> "DETAIL".equals(image.getPurpose()))
                .map(Image::getImgOrgName)
                .toList()).containsExactlyInAnyOrder("img1.jpg", "img2.jpg", "img3.jpg");

        // 이미지 공통 검증
        assertThat(savedImages).allSatisfy(image -> {
            assertThat(image.getImgStoredName()).isNotBlank();
            assertThat(image.getImgPath()).contains(image.getImgStoredName());
            assertThat(image.getPurpose()).isIn("REPRESENTATIVE", "DETAIL");
        });

        // 지갑 상태 변경 검증
        Wallet usedWallet = walletRepository.findById(savedCampaign.getWalletNo())
                .orElseThrow(() -> new IllegalArgumentException("지갑을 찾을 수 없습니다."));

        // 사용된 지갑은 ACTIVE로 변경되어야 함
        assertThat(usedWallet.getStatus()).isEqualTo(WalletStatus.ACTIVE);

        // INACTIVE 지갑 개수 감소 확인
        long afterInactiveCount = walletRepository.findAll().stream()
                .filter(w -> myWallets.contains(w.getWalletAddress()) && w.getStatus() == WalletStatus.INACTIVE)
                .count();

        assertThat(afterInactiveCount).isEqualTo(beforeInactiveCount - 1);
    }
}
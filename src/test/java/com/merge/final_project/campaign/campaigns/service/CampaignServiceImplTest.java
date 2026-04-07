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
    @DisplayName("캠페인 등록 시 본체, 지출 계획, 승인 대기 상태 및 지갑 활성화 확인")
    void registerCampaign_success() {
        Long organizationId = 888L;

        // 테스트 실행 시마다 제목이 겹치지 않게 현재 시간 추가
        String campaignTitle = "깨끗한 식수 지원 사업-" + System.currentTimeMillis();

        List<String> myWallets = List.of(
                "0x888_BUSY_ADDR",
                "0x888_FREE_ADDR_1",
                "0x888_FREE_ADDR_2"
        );

        // 등록 전 비활성화 지갑 개수 확인
        long beforeInactiveCount = walletRepository.findAll().stream()
                .filter(w -> myWallets.contains(w.getWalletAddress()) && w.getStatus() == WalletStatus.INACTIVE)
                .count();

        // 주의: DB에 해당 주소의 INACTIVE 지갑이 미리 존재해야 합니다.
        assertThat(beforeInactiveCount).isGreaterThan(0);

        // 캠페인 등록 정보 입력 (DTO)
        CampaignRequestDto dto = new CampaignRequestDto();
        dto.setTitle(campaignTitle);
        dto.setDescription("아프리카 지역의 우물 굴착 및 식수 시설 구축 프로젝트입니다.");
        dto.setTargetAmount(2000000L);

        // 기간 설정
        dto.setStartAt(LocalDateTime.of(2025, 1, 1, 0, 0));
        dto.setEndAt(LocalDateTime.of(2025, 3, 31, 23, 59, 59));
        dto.setUsageStartAt(LocalDateTime.of(2025, 4, 1, 0, 0));
        dto.setUsageEndAt(LocalDateTime.of(2025, 6, 30, 23, 59, 59));

        // 테스트용 이미지 파일 생성
        MockMultipartFile imageFile = new MockMultipartFile(
                "imageFile",
                "images.png",
                "image/png",
                "test-image".getBytes()
        );

        // 지출 계획 데이터 구성
        UsePlanRequestDto plan1 = new UsePlanRequestDto();
        plan1.setPlanContent("우물 굴착 장비 대여비");
        plan1.setPlanAmount(1500000L);

        UsePlanRequestDto plan2 = new UsePlanRequestDto();
        plan2.setPlanContent("현지 활동가 활동비 및 식비");
        plan2.setPlanAmount(500000L);

        dto.setUsePlans(List.of(plan1, plan2));

        // 서비스 로직 실행
        campaignService.registerCampaign(dto, imageFile, organizationId);

        // 캠페인 본체 저장 확인
        Campaign savedCampaign = campaignRepository.findAll().stream()
                .filter(c -> campaignTitle.equals(c.getTitle()))
                .filter(c -> organizationId.equals(c.getFoundationNo()))
                .max((left, right) -> Long.compare(left.getCampaignNo(), right.getCampaignNo()))
                .orElseThrow(() -> new IllegalArgumentException("캠페인이 생성되지 않았습니다."));

        assertThat(savedCampaign.getTitle()).isEqualTo(campaignTitle);
        assertThat(savedCampaign.getImagePath()).isNull(); // 이미지 테이블을 따로 쓰므로 본체 경로는 null
        assertThat(savedCampaign.getApprovalStatus()).isEqualTo(ApprovalStatus.PENDING);

        // 기간 저장 검증
        assertThat(savedCampaign.getStartAt()).isEqualTo(LocalDateTime.of(2025, 1, 1, 0, 0));
        assertThat(savedCampaign.getEndAt()).isEqualTo(LocalDateTime.of(2025, 3, 31, 23, 59, 59));
        assertThat(savedCampaign.getUsageStartAt()).isEqualTo(LocalDateTime.of(2025, 4, 1, 0, 0));
        assertThat(savedCampaign.getUsageEndAt()).isEqualTo(LocalDateTime.of(2025, 6, 30, 23, 59, 59));

        // 지출 계획 저장 확인
        List<UsePlan> savedPlans = usePlanRepository.findAll().stream()
                .filter(plan -> savedCampaign.getCampaignNo().equals(plan.getCampaignNo()))
                .toList();

        assertThat(savedPlans).hasSize(2);
        assertThat(savedPlans).extracting("planContent")
                .containsExactlyInAnyOrder("우물 굴착 장비 대여비", "현지 활동가 활동비 및 식비");

        // 이미지 테이블 저장 확인
        List<Image> savedImages = imageRepository.findByTargetNameAndTargetNo("campaign", savedCampaign.getCampaignNo());

        assertThat(savedImages).hasSize(1);
        assertThat(savedImages.get(0).getImgOrgName()).isEqualTo("images.png");
        assertThat(savedImages.get(0).getImgStoredName()).contains("_images.png");
        assertThat(savedImages.get(0).getImgPath()).contains(savedImages.get(0).getImgStoredName());

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
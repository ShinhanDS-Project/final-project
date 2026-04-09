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
import com.merge.final_project.wallet.entity.WalletRepository;
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
    @DisplayName("??? ?? ? ??, ????, ???, ?? ??? ????")
    void registerCampaign_success() {
        Long foundationId = 888L;

        String campaignTitle = "??? ?? ?? ??-" + System.currentTimeMillis();

        List<String> myWallets = List.of(
                "0x888_BUSY_ADDR",
                "0x888_FREE_ADDR_1",
                "0x888_FREE_ADDR_2"
        );

        long beforeInactiveCount = walletRepository.findAll().stream()
                .filter(w -> myWallets.contains(w.getWalletAddress()) && w.getStatus() == WalletStatus.INACTIVE)
                .count();

        assertThat(beforeInactiveCount).isGreaterThan(0);

        CampaignRequestDTO dto = new CampaignRequestDTO();
        dto.setTitle(campaignTitle);
        dto.setDescription("???? ??? ?? ?? ? ?? ?? ?? ???????.");
        dto.setCategory("??");
        dto.setTargetAmount(2_000_000L);
        dto.setStartAt(LocalDateTime.of(2025, 1, 1, 0, 0));
        dto.setEndAt(LocalDateTime.of(2025, 3, 31, 23, 59, 59));
        dto.setUsageStartAt(LocalDateTime.of(2025, 4, 1, 0, 0));
        dto.setUsageEndAt(LocalDateTime.of(2025, 6, 30, 23, 59, 59));

        MockMultipartFile imageFile = new MockMultipartFile(
                "imageFile",
                "images.png",
                "image/png",
                "test-image".getBytes()
        );

        UsePlanRequestDTO plan1 = new UsePlanRequestDTO();
        plan1.setPlanContent("?? ?? ?? ???");
        plan1.setPlanAmount(1_500_000L);

        UsePlanRequestDTO plan2 = new UsePlanRequestDTO();
        plan2.setPlanContent("?? ??? ? ???");
        plan2.setPlanAmount(500_000L);

        dto.setUsePlans(List.of(plan1, plan2));

        campaignService.registerCampaign(dto, imageFile, List.of(), foundationId);

        Campaign savedCampaign = campaignRepository.findAll().stream()
                .filter(c -> campaignTitle.equals(c.getTitle()))
                .filter(c -> foundationId.equals(c.getFoundationNo()))
                .max((left, right) -> Long.compare(left.getCampaignNo(), right.getCampaignNo()))
                .orElseThrow(() -> new IllegalArgumentException("???? ???? ?????."));

        assertThat(savedCampaign.getTitle()).isEqualTo(campaignTitle);
        assertThat(savedCampaign.getImagePath()).isNull();
        assertThat(savedCampaign.getApprovalStatus()).isEqualTo(ApprovalStatus.PENDING);
        assertThat(savedCampaign.getCampaignStatus()).isEqualTo(CampaignStatus.PENDING);
        assertThat(savedCampaign.getCurrentAmount()).isEqualTo(0L);

        assertThat(savedCampaign.getStartAt()).isEqualTo(LocalDateTime.of(2025, 1, 1, 0, 0));
        assertThat(savedCampaign.getEndAt()).isEqualTo(LocalDateTime.of(2025, 3, 31, 23, 59, 59));
        assertThat(savedCampaign.getUsageStartAt()).isEqualTo(LocalDateTime.of(2025, 4, 1, 0, 0));
        assertThat(savedCampaign.getUsageEndAt()).isEqualTo(LocalDateTime.of(2025, 6, 30, 23, 59, 59));

        List<UsePlan> savedPlans = usePlanRepository.findAll().stream()
                .filter(plan -> savedCampaign.getCampaignNo().equals(plan.getCampaignNo()))
                .toList();

        assertThat(savedPlans).hasSize(2);
        assertThat(savedPlans).extracting("planContent")
                .containsExactlyInAnyOrder("?? ?? ?? ???", "?? ??? ? ???");

        List<Image> savedImages = imageRepository.findByTargetNameAndTargetNo("campaign", savedCampaign.getCampaignNo());

        assertThat(savedImages).hasSize(1);
        assertThat(savedImages.get(0).getImgOrgName()).isEqualTo("images.png");
        assertThat(savedImages.get(0).getImgStoredName()).contains("_images.png");
        assertThat(savedImages.get(0).getImgPath()).contains(savedImages.get(0).getImgStoredName());

        Wallet usedWallet = walletRepository.findById(savedCampaign.getWalletNo())
                .orElseThrow(() -> new IllegalArgumentException("??? ??? ?? ? ????."));

        assertThat(usedWallet.getStatus()).isEqualTo(WalletStatus.ACTIVE);

        long afterInactiveCount = walletRepository.findAll().stream()
                .filter(w -> myWallets.contains(w.getWalletAddress()) && w.getStatus() == WalletStatus.INACTIVE)
                .count();

        assertThat(afterInactiveCount).isEqualTo(beforeInactiveCount - 1);
    }
}

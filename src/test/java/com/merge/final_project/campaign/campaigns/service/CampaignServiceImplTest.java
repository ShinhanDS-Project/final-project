package com.merge.final_project.campaign.campaigns.service;

import com.merge.final_project.campaign.campaigns.ApprovalStatus;
import com.merge.final_project.campaign.campaigns.CampaignStatus;
import com.merge.final_project.campaign.campaigns.dto.CampaignRequestDTO;
import com.merge.final_project.campaign.campaigns.entity.Campaign;
import com.merge.final_project.campaign.campaigns.repository.CampaignRepository;
import com.merge.final_project.campaign.useplan.dto.UsePlanRequestDTO;
import com.merge.final_project.global.Image;
import com.merge.final_project.global.ImageRepository;
import com.merge.final_project.global.utils.FileUtil;
import com.merge.final_project.org.foundation.Foundation;
import com.merge.final_project.org.foundation.FoundationRepository;
import com.merge.final_project.wallet.entity.Wallet;
import com.merge.final_project.wallet.entity.WalletStatus;
import com.merge.final_project.wallet.entity.WalletType;
import com.merge.final_project.wallet.repository.WalletRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = "blockchain.rpc.url=http://localhost:8545")
@Transactional
class CampaignServiceImplTest {

    @Autowired
    private CampaignService campaignService;

    @Autowired
    private CampaignRepository campaignRepository;

    @Autowired
    private FoundationRepository foundationRepository;

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private ImageRepository imageRepository;

    @Test
    @DisplayName("registerCampaign uses inactive wallet and saves campaign images")
    void registerCampaign_success() {
        Wallet foundationWallet = walletRepository.save(Wallet.builder()
                .walletType(WalletType.FOUNDATION)
                .ownerNo(0L)
                .walletAddress("foundation-main-" + System.nanoTime())
                .balance(BigDecimal.ZERO)
                .walletHash("foundation-hash")
                .status(WalletStatus.ACTIVE)
                .build());

        String activeAddress = "campaign-active-" + System.nanoTime();
        String inactiveAddress = "campaign-inactive-" + System.nanoTime();
        String reserveAddress = "campaign-reserve-" + System.nanoTime();

        Foundation foundation = foundationRepository.save(Foundation.builder()
                .foundationEmail("foundation-" + System.nanoTime() + "@test.com")
                .foundationName("Hope Foundation")
                .campaignWallet1(activeAddress)
                .campaignWallet2(inactiveAddress)
                .campaignWallet3(reserveAddress)
                .wallet(foundationWallet)
                .build());

        walletRepository.save(Wallet.builder()
                .walletType(WalletType.FOUNDATION)
                .ownerNo(foundation.getFoundationNo())
                .walletAddress(activeAddress)
                .balance(BigDecimal.ZERO)
                .walletHash("active-hash")
                .status(WalletStatus.ACTIVE)
                .build());

        Wallet inactiveWallet = walletRepository.save(Wallet.builder()
                .walletType(WalletType.FOUNDATION)
                .ownerNo(foundation.getFoundationNo())
                .walletAddress(inactiveAddress)
                .balance(BigDecimal.ZERO)
                .walletHash("inactive-hash")
                .status(WalletStatus.INACTIVE)
                .build());

        walletRepository.save(Wallet.builder()
                .walletType(WalletType.FOUNDATION)
                .ownerNo(foundation.getFoundationNo())
                .walletAddress(reserveAddress)
                .balance(BigDecimal.ZERO)
                .walletHash("reserve-hash")
                .status(WalletStatus.ACTIVE)
                .build());

        CampaignRequestDTO dto = new CampaignRequestDTO();
        dto.setTitle("campaign-test-" + System.nanoTime());
        dto.setDescription("campaign register integration test");
        dto.setCategory("medical");
        dto.setTargetAmount(2_000_000L);
        dto.setStartAt(LocalDateTime.of(2026, 4, 1, 0, 0));
        dto.setEndAt(LocalDateTime.of(2026, 4, 30, 23, 59, 59));
        dto.setUsageStartAt(LocalDateTime.of(2026, 5, 1, 0, 0));
        dto.setUsageEndAt(LocalDateTime.of(2026, 5, 31, 23, 59, 59));

        UsePlanRequestDTO usePlan = new UsePlanRequestDTO();
        usePlan.setPlanContent("medical support");
        usePlan.setPlanAmount(2_000_000L);
        dto.setUsePlans(List.of(usePlan));

        MockMultipartFile representativeImage = new MockMultipartFile(
                "imageFile",
                "thumb.png",
                "image/png",
                "thumb-image".getBytes()
        );

        MockMultipartFile detailImage1 = new MockMultipartFile(
                "detailImageFiles",
                "detail-1.jpg",
                "image/jpeg",
                "detail-image-1".getBytes()
        );

        MockMultipartFile detailImage2 = new MockMultipartFile(
                "detailImageFiles",
                "detail-2.jpg",
                "image/jpeg",
                "detail-image-2".getBytes()
        );

        campaignService.registerCampaign(
                dto,
                representativeImage,
                List.of(detailImage1, detailImage2),
                foundation.getFoundationNo()
        );

        Campaign savedCampaign = campaignRepository.findAll().stream()
                .filter(campaign -> foundation.getFoundationNo().equals(campaign.getFoundationNo()))
                .filter(campaign -> dto.getTitle().equals(campaign.getTitle()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("campaign not saved"));

        assertThat(savedCampaign.getFoundationNo()).isEqualTo(foundation.getFoundationNo());
        assertThat(savedCampaign.getWalletNo()).isEqualTo(inactiveWallet.getWalletNo());
        assertThat(savedCampaign.getApprovalStatus()).isEqualTo(ApprovalStatus.PENDING);
        assertThat(savedCampaign.getCampaignStatus()).isEqualTo(CampaignStatus.PENDING);
        assertThat(savedCampaign.getCurrentAmount()).isZero();

        Wallet usedWallet = walletRepository.findByWalletNo(inactiveWallet.getWalletNo())
                .orElseThrow(() -> new AssertionError("wallet not found"));

        assertThat(usedWallet.getStatus()).isEqualTo(WalletStatus.ACTIVE);

        List<Image> savedImages = imageRepository.findByTargetNameAndTargetNo("campaign", savedCampaign.getCampaignNo());

        assertThat(savedImages).hasSize(3);
        assertThat(savedImages).extracting(Image::getImgOrgName)
                .containsExactlyInAnyOrder("thumb.png", "detail-1.jpg", "detail-2.jpg");
        assertThat(savedImages).extracting(Image::getPurpose)
                .containsExactlyInAnyOrder("REPRESENTATIVE", "DETAIL", "DETAIL");
        assertThat(savedImages).extracting(Image::getImgStoredName)
                .containsExactlyInAnyOrder(
                        "stored-thumb.png",
                        "stored-detail-1.jpg",
                        "stored-detail-2.jpg"
                );
        assertThat(savedImages).allSatisfy(image -> {
            assertThat(image.getTargetName()).isEqualTo("campaign");
            assertThat(image.getTargetNo()).isEqualTo(savedCampaign.getCampaignNo());
            assertThat(image.getImgPath()).startsWith("C:/uploads/");
        });
    }

    @TestConfiguration
    static class FileUtilTestConfig {

        @Bean
        @Primary
        FileUtil fileUtil() {
            return new FileUtil() {
                @Override
                public String saveFile(MultipartFile file) throws IOException {
                    return "stored-" + file.getOriginalFilename();
                }
            };
        }
    }
}

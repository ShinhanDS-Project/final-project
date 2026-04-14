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
import com.merge.final_project.org.Foundation;
import com.merge.final_project.org.FoundationRepository;
import com.merge.final_project.wallet.entity.Wallet;
import com.merge.final_project.wallet.entity.WalletStatus;
import com.merge.final_project.wallet.repository.WalletRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class CampaignServiceImplTest {

    private static final Long FOUNDATION_NO = 10000L;
    private static final Long BENEFICIARY_NO = 10000L;
    private static final String BENEFICIARY_ENTRY_CODE = "12345678";

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

    @Autowired
    private FoundationRepository foundationRepository;

    @Autowired
    private S3Client s3Client;

    @Value("${cloud.aws.s3.bucket}")
    private String bucketName;

    @Test
    @Transactional
    @Rollback(false)
    @DisplayName("foundation 10000의 현재 지갑 상태 기준으로 캠페인 등록 성공 또는 비활성 지갑 없음 에러를 검증한다")
    void registerCampaign_withExistingFoundationData() {
        Foundation foundation = foundationRepository.findByFoundationNo(FOUNDATION_NO)
                .orElseThrow(() -> new IllegalArgumentException("foundation 484가 존재하지 않습니다."));

        List<String> campaignWalletAddresses = Stream.of(
                foundation.getCampaignWallet1(),
                foundation.getCampaignWallet2(),
                foundation.getCampaignWallet3()
        ).filter(Objects::nonNull).toList();

        String campaignTitle = "올드페리도넛" + System.currentTimeMillis();

        CampaignRequestDTO dto = new CampaignRequestDTO();
        dto.setTitle(campaignTitle);
        dto.setEntryCode(BENEFICIARY_ENTRY_CODE);
        dto.setDescription("올드페리도넛 먹은날");
        dto.setCategory(com.merge.final_project.campaign.campaigns.CampaignCategory.ETC);
        dto.setTargetAmount(3_000_000L);
        dto.setStartAt(LocalDateTime.of(2026, 4, 10, 10, 0));
        dto.setEndAt(LocalDateTime.of(2026, 5, 10, 18, 0));
        dto.setUsageStartAt(LocalDateTime.of(2026, 5, 11, 9, 0));
        dto.setUsageEndAt(LocalDateTime.of(2026, 6, 30, 18, 0));

        UsePlanRequestDTO plan1 = new UsePlanRequestDTO();
        plan1.setPlanContent("초코");
        plan1.setPlanAmount(2_000_000L);

        UsePlanRequestDTO plan2 = new UsePlanRequestDTO();
        plan2.setPlanContent("크런치");
        plan2.setPlanAmount(1_000_000L);

        dto.setUsePlans(List.of(plan1, plan2));

        MockMultipartFile representativeImage = new MockMultipartFile(
                "imageFile",
                "representative.png",
                "image/png",
                "representative-image".getBytes()
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

        List<Wallet> trackedWallets = campaignWalletAddresses.stream()
                .map(walletRepository::findByWalletAddress)
                .flatMap(Optional -> Optional.stream())
                .toList();

        boolean hasInactiveWallet = trackedWallets.stream()
                .anyMatch(wallet -> wallet.getStatus() == WalletStatus.INACTIVE);

        if (!hasInactiveWallet) {
            assertThatThrownBy(() ->
                    campaignService.registerCampaign(
                            dto,
                            representativeImage,
                            List.of(detailImage1, detailImage2),
                            FOUNDATION_NO
                    )
            ).isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("사용 가능한 지갑이 없습니다.");
            return;
        }

        long beforeInactiveCount = trackedWallets.stream()
                .filter(wallet -> wallet.getStatus() == WalletStatus.INACTIVE)
                .count();

        campaignService.registerCampaign(
                dto,
                representativeImage,
                List.of(detailImage1, detailImage2),
                FOUNDATION_NO
        );

        Campaign savedCampaign = campaignRepository.findAll().stream()
                .filter(campaign -> campaignTitle.equals(campaign.getTitle()))
                .filter(campaign -> FOUNDATION_NO.equals(campaign.getFoundationNo()))
                .max(Comparator.comparing(Campaign::getCampaignNo))
                .orElseThrow(() -> new IllegalArgumentException("캠페인이 저장되지 않았습니다."));

        assertThat(savedCampaign.getFoundationNo()).isEqualTo(FOUNDATION_NO);
        assertThat(savedCampaign.getBeneficiaryNo()).isEqualTo(BENEFICIARY_NO);
        assertThat(savedCampaign.getTitle()).isEqualTo(campaignTitle);
        assertThat(savedCampaign.getApprovalStatus()).isEqualTo(ApprovalStatus.PENDING);
        assertThat(savedCampaign.getCampaignStatus()).isEqualTo(CampaignStatus.PENDING);
        assertThat(savedCampaign.getCurrentAmount()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(savedCampaign.getTargetAmount()).isEqualTo(3_000_000L);

        Wallet matchedWallet = walletRepository.findById(savedCampaign.getWalletNo())
                .orElseThrow(() -> new IllegalArgumentException("매칭된 지갑을 찾을 수 없습니다."));

        assertThat(campaignWalletAddresses).contains(matchedWallet.getWalletAddress());
        assertThat(matchedWallet.getStatus()).isEqualTo(WalletStatus.ACTIVE);

        long afterInactiveCount = campaignWalletAddresses.stream()
                .map(walletRepository::findByWalletAddress)
                .flatMap(Optional -> Optional.stream())
                .filter(wallet -> wallet.getStatus() == WalletStatus.INACTIVE)
                .count();

        assertThat(afterInactiveCount).isEqualTo(beforeInactiveCount - 1);

        List<UsePlan> savedPlans = usePlanRepository.findAll().stream()
                .filter(plan -> savedCampaign.getCampaignNo().equals(plan.getCampaignNo()))
                .toList();

        assertThat(savedPlans).hasSize(2);
        assertThat(savedPlans).extracting(UsePlan::getPlanContent)
                .containsExactlyInAnyOrder("초코", "크런치");
        assertThat(savedPlans).extracting(UsePlan::getPlanAmount)
                .containsExactlyInAnyOrder(2_000_000L, 1_000_000L);

        List<Image> savedImages = imageRepository.findByTargetNameAndTargetNo("campaign", savedCampaign.getCampaignNo());

        assertThat(savedImages).hasSize(3);
        assertThat(savedImages).extracting(Image::getPurpose)
                .containsExactlyInAnyOrder("REPRESENTATIVE", "DETAIL", "DETAIL");

        Image savedRepresentativeImage = savedImages.stream()
                .filter(image -> "REPRESENTATIVE".equals(image.getPurpose()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("대표 이미지가 저장되지 않았습니다."));

        assertThat(savedRepresentativeImage.getImgOrgName()).isEqualTo("representative.png");
        assertThat(savedRepresentativeImage.getImgStoredName()).contains("_representative.png");
        assertThat(savedRepresentativeImage.getImgPath()).startsWith("https://");
        assertThat(savedRepresentativeImage.getImgPath()).contains(".amazonaws.com/");
        assertThat(savedRepresentativeImage.getImgPath()).endsWith(savedRepresentativeImage.getImgStoredName());

        s3Client.headObject(HeadObjectRequest.builder()
                .bucket(bucketName)
                .key(savedRepresentativeImage.getImgStoredName())
                .build());

        List<Image> savedDetailImages = savedImages.stream()
                .filter(image -> "DETAIL".equals(image.getPurpose()))
                .sorted(Comparator.comparing(Image::getImgOrgName))
                .toList();

        assertThat(savedDetailImages).extracting(Image::getImgOrgName)
                .containsExactly("detail-1.png", "detail-2.png");

        savedDetailImages.forEach(image -> {
            assertThat(image.getImgStoredName()).contains("_detail-");
            assertThat(image.getImgPath()).startsWith("https://");
            assertThat(image.getImgPath()).contains(".amazonaws.com/");

            s3Client.headObject(HeadObjectRequest.builder()
                    .bucket(bucketName)
                    .key(image.getImgStoredName())
                    .build());
        });
    }
}

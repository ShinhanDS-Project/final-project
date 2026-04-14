package com.merge.final_project.campaign.campaigns.service;

import com.merge.final_project.campaign.campaigns.CampaignCategory;
import com.merge.final_project.campaign.campaigns.CampaignStatus;
import com.merge.final_project.campaign.campaigns.dto.CampaignListResponseDTO;
import com.merge.final_project.campaign.campaigns.entity.Campaign;
import com.merge.final_project.campaign.campaigns.repository.CampaignRepository;
import com.merge.final_project.campaign.useplan.repository.UsePlanRepository;
import com.merge.final_project.global.Image;
import com.merge.final_project.global.ImageRepository;
import com.merge.final_project.global.utils.FileService;
import com.merge.final_project.org.Foundation;
import com.merge.final_project.org.FoundationRepository;
import com.merge.final_project.wallet.repository.WalletRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CampaignListServiceTest {

    @Mock
    private FoundationRepository foundationRepository;

    @Mock
    private WalletRepository walletRepository;

    @Mock
    private CampaignRepository campaignRepository;

    @Mock
    private UsePlanRepository usePlanRepository;

    @Mock
    private ImageRepository imageRepository;

    @Mock
    private FileService fileService;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private CampaignServiceImpl campaignService;

    @Test
    @DisplayName("마감임박 조회 시 종료일 오름차순으로 캠페인 목록을 반환한다")
    void getCampaignList_deadline() {
        Foundation foundation = Foundation.builder()
                .foundationNo(1L)
                .foundationName("희망 재단")
                .build();

        Campaign first = Campaign.builder()
                .campaignNo(10L)
                .title("긴급 구호 캠페인")
                .targetAmount(1_000_000L)
                .currentAmount(BigDecimal.valueOf(300_000L))
                .category(CampaignCategory.ETC)
                .endAt(LocalDateTime.of(2026, 4, 10, 0, 0))
                .foundationNo(1L)
                .campaignStatus(CampaignStatus.RECRUITING)
                .build();

        Campaign second = Campaign.builder()
                .campaignNo(11L)
                .title("유기견 보호 캠페인")
                .targetAmount(2_000_000L)
                .currentAmount(BigDecimal.valueOf(700_000L))
                .category(CampaignCategory.ANIMAL)
                .endAt(LocalDateTime.of(2026, 4, 12, 0, 0))
                .foundationNo(1L)
                .campaignStatus(CampaignStatus.RECRUITING)
                .build();

        when(campaignRepository.findAll()).thenReturn(List.of(second, first));
        when(foundationRepository.findByFoundationNo(1L)).thenReturn(Optional.of(foundation));
        when(imageRepository.findByTargetNameAndTargetNo("campaign", 10L))
                .thenReturn(List.of(
                        Image.builder()
                                .targetNo(10L)
                                .imgPath("/images/10-new.png")
                                .imgOrgName("10-new.png")
                                .imgStoredName("10-new.png")
                                .targetName("campaign")
                                .purpose("REPRESENTATIVE")
                                .createdAt(LocalDateTime.of(2026, 4, 2, 10, 0))
                                .build()
                ));
        when(imageRepository.findByTargetNameAndTargetNo("campaign", 11L))
                .thenReturn(List.of(
                        Image.builder()
                                .targetNo(11L)
                                .imgPath("/images/11.png")
                                .imgOrgName("11.png")
                                .imgStoredName("11.png")
                                .targetName("campaign")
                                .purpose("REPRESENTATIVE")
                                .createdAt(LocalDateTime.of(2026, 4, 1, 11, 0))
                                .build()
                ));

        List<CampaignListResponseDTO> result = campaignService.getCampaignList("deadline", null, null, null);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getCampaignNo()).isEqualTo(10L);
        assertThat(result.get(0).getImagePath()).isEqualTo("/images/10-new.png");
        assertThat(result.get(0).getFoundationName()).isEqualTo("희망 재단");
        assertThat(result.get(1).getCampaignNo()).isEqualTo(11L);

        verify(campaignRepository).findAll();
    }

    @Test
    @DisplayName("참여순 조회 시 모금액 내림차순으로 캠페인 목록을 반환한다")
    void getCampaignList_participation() {
        Foundation foundation = Foundation.builder()
                .foundationNo(2L)
                .foundationName("환경 단체")
                .build();

        Campaign lowAmount = Campaign.builder()
                .campaignNo(20L)
                .title("숲 복원")
                .targetAmount(1_000_000L)
                .currentAmount(BigDecimal.valueOf(500_000L))
                .category(CampaignCategory.ENVIRONMENT)
                .endAt(LocalDateTime.of(2026, 4, 20, 0, 0))
                .foundationNo(2L)
                .campaignStatus(CampaignStatus.RECRUITING)
                .build();

        Campaign highAmount = Campaign.builder()
                .campaignNo(21L)
                .title("강 조성 사업")
                .targetAmount(3_000_000L)
                .currentAmount(BigDecimal.valueOf(2_500_000L))
                .category(CampaignCategory.ENVIRONMENT)
                .endAt(LocalDateTime.of(2026, 4, 22, 0, 0))
                .foundationNo(2L)
                .campaignStatus(CampaignStatus.RECRUITING)
                .build();

        when(campaignRepository.findAll()).thenReturn(List.of(lowAmount, highAmount));
        when(foundationRepository.findByFoundationNo(2L)).thenReturn(Optional.of(foundation));
        when(imageRepository.findByTargetNameAndTargetNo("campaign", 20L)).thenReturn(List.of());
        when(imageRepository.findByTargetNameAndTargetNo("campaign", 21L))
                .thenReturn(List.of(
                        Image.builder()
                                .targetNo(21L)
                                .imgPath("/images/21.png")
                                .imgOrgName("21.png")
                                .imgStoredName("21.png")
                                .targetName("campaign")
                                .purpose("REPRESENTATIVE")
                                .createdAt(LocalDateTime.of(2026, 4, 3, 9, 0))
                                .build()
                ));

        List<CampaignListResponseDTO> result = campaignService.getCampaignList("participation", null, null, null);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getCampaignNo()).isEqualTo(21L);
        assertThat(result.get(0).getCurrentAmount()).isEqualByComparingTo("2500000");
        assertThat(result.get(0).getImagePath()).isEqualTo("/images/21.png");
        assertThat(result.get(0).getFoundationName()).isEqualTo("환경 단체");
        assertThat(result.get(1).getCampaignNo()).isEqualTo(20L);

        verify(campaignRepository).findAll();
    }
}

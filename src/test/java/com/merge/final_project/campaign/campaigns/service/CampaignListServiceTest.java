package com.merge.final_project.campaign.campaigns.service;

import com.merge.final_project.campaign.campaigns.CampaignStatus;
import com.merge.final_project.campaign.campaigns.dto.CampaignListResponseDTO;
import com.merge.final_project.campaign.campaigns.entity.Campaign;
import com.merge.final_project.campaign.campaigns.repository.CampaignRepository;
import com.merge.final_project.campaign.useplan.repository.UsePlanRepository;
import com.merge.final_project.global.Image;
import com.merge.final_project.global.ImageRepository;
import com.merge.final_project.global.utils.FileUtil;
import com.merge.final_project.org.Foundation;
import com.merge.final_project.org.FoundationRepository;
import com.merge.final_project.wallet.entity.WalletRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

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
    private FileUtil fileUtil;

    @InjectMocks
    private CampaignServiceImpl campaignService;

    @Test
    @DisplayName("???? ?? ? RECRUITING ???? ??? ?????? ????")
    void getCampaignList_deadline() {
        Foundation foundation = Foundation.builder()
                .foundationNo(1L)
                .foundationName("?? ??")
                .build();

        Campaign first = Campaign.builder()
                .campaignNo(10L)
                .title("?? ?? ??")
                .targetAmount(1_000_000L)
                .currentAmount(300_000L)
                .category("??")
                .endAt(LocalDateTime.of(2026, 4, 10, 0, 0))
                .foundationNo(1L)
                .campaignStatus(CampaignStatus.RECRUITING)
                .build();
        first.setFoundation(foundation);

        Campaign second = Campaign.builder()
                .campaignNo(11L)
                .title("?? ?? ??")
                .targetAmount(2_000_000L)
                .currentAmount(700_000L)
                .category("??")
                .endAt(LocalDateTime.of(2026, 4, 12, 0, 0))
                .foundationNo(1L)
                .campaignStatus(CampaignStatus.RECRUITING)
                .build();
        second.setFoundation(foundation);

        when(campaignRepository.findByCampaignStatusOrderByEndAtAscCampaignNoDesc(CampaignStatus.RECRUITING))
                .thenReturn(List.of(first, second));

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
                                .build(),
                        Image.builder()
                                .targetNo(10L)
                                .imgPath("/images/10-old.png")
                                .imgOrgName("10-old.png")
                                .imgStoredName("10-old.png")
                                .targetName("campaign")
                                .purpose("REPRESENTATIVE")
                                .createdAt(LocalDateTime.of(2026, 4, 1, 10, 0))
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

        List<CampaignListResponseDTO> result = campaignService.getCampaignList("deadline");

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getCampaignNo()).isEqualTo(10L);
        assertThat(result.get(0).getImagePath()).isEqualTo("/images/10-new.png");
        assertThat(result.get(0).getFoundationName()).isEqualTo("?? ??");
        assertThat(result.get(0).getEndAt()).isEqualTo(LocalDateTime.of(2026, 4, 10, 0, 0));
        assertThat(result.get(1).getCampaignNo()).isEqualTo(11L);

        verify(campaignRepository).findByCampaignStatusOrderByEndAtAscCampaignNoDesc(CampaignStatus.RECRUITING);
    }

    @Test
    @DisplayName("??? ?? ? RECRUITING ???? ?? ??? ?????? ????")
    void getCampaignList_participation() {
        Foundation foundation = Foundation.builder()
                .foundationNo(2L)
                .foundationName("?? ??")
                .build();

        Campaign campaign = Campaign.builder()
                .campaignNo(21L)
                .title("?? ??")
                .targetAmount(3_000_000L)
                .currentAmount(2_500_000L)
                .category("??")
                .endAt(LocalDateTime.of(2026, 4, 20, 0, 0))
                .foundationNo(2L)
                .campaignStatus(CampaignStatus.RECRUITING)
                .build();
        campaign.setFoundation(foundation);

        when(campaignRepository.findByCampaignStatusOrderByCurrentAmountDescCampaignNoDesc(CampaignStatus.RECRUITING))
                .thenReturn(List.of(campaign));

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

        List<CampaignListResponseDTO> result = campaignService.getCampaignList("participation");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCampaignNo()).isEqualTo(21L);
        assertThat(result.get(0).getCurrentAmount()).isEqualTo(2_500_000L);
        assertThat(result.get(0).getImagePath()).isEqualTo("/images/21.png");
        assertThat(result.get(0).getFoundationName()).isEqualTo("?? ??");

        verify(campaignRepository).findByCampaignStatusOrderByCurrentAmountDescCampaignNoDesc(CampaignStatus.RECRUITING);
    }
}

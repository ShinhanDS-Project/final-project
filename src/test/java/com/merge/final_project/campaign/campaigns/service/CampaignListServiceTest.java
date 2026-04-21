package com.merge.final_project.campaign.campaigns.service;

import com.merge.final_project.campaign.campaigns.ApprovalStatus;
import com.merge.final_project.campaign.campaigns.CampaignCategory;
import com.merge.final_project.campaign.campaigns.CampaignStatus;
import com.merge.final_project.campaign.campaigns.dto.CampaignListPageResponseDTO;
import com.merge.final_project.campaign.campaigns.entity.Campaign;
import com.merge.final_project.campaign.campaigns.repository.CampaignRepository;
import com.merge.final_project.campaign.useplan.repository.UsePlanRepository;
import com.merge.final_project.donation.donations.DonationRepository;
import com.merge.final_project.global.Image;
import com.merge.final_project.global.ImageRepository;
import com.merge.final_project.global.utils.FileService;
import com.merge.final_project.org.Foundation;
import com.merge.final_project.org.FoundationRepository;
import com.merge.final_project.recipient.beneficiary.repository.BeneficiaryRepository;
import com.merge.final_project.user.users.UserRepository;
import com.merge.final_project.wallet.repository.WalletRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CampaignListServiceTest {

    @Mock
    private FoundationRepository foundationRepository;

    @Mock
    private BeneficiaryRepository beneficiaryRepository;

    @Mock
    private WalletRepository walletRepository;

    @Mock
    private CampaignRepository campaignRepository;

    @Mock
    private UsePlanRepository usePlanRepository;

    @Mock
    private DonationRepository donationRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ImageRepository imageRepository;

    @Mock
    private FileService fileService;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private CampaignServiceImpl campaignService;

    @Test
    @DisplayName("공개 캠페인 목록은 기본 ACTIVE 상태만 페이지로 반환한다")
    void getCampaignList_activeOnlyPaged() {
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
                .startAt(LocalDateTime.of(2026, 4, 1, 0, 0))
                .endAt(LocalDateTime.of(2026, 4, 10, 0, 0))
                .foundationNo(1L)
                .foundation(foundation)
                .campaignStatus(CampaignStatus.ACTIVE)
                .build();

        Campaign second = Campaign.builder()
                .campaignNo(11L)
                .title("유기견 보호 캠페인")
                .targetAmount(2_000_000L)
                .currentAmount(BigDecimal.valueOf(700_000L))
                .category(CampaignCategory.ANIMAL)
                .startAt(LocalDateTime.of(2026, 4, 2, 0, 0))
                .endAt(LocalDateTime.of(2026, 4, 12, 0, 0))
                .foundationNo(1L)
                .foundation(foundation)
                .campaignStatus(CampaignStatus.ACTIVE)
                .build();

        PageRequest pageRequest = PageRequest.of(0, 6, Sort.by(Sort.Order.asc("endAt"), Sort.Order.desc("campaignNo")));
        when(campaignRepository.findPublicCampaignPage(
                eq(ApprovalStatus.APPROVED),
                any(),
                eq(null),
                eq(""),
                eq(pageRequest)
        )).thenReturn(new PageImpl<>(List.of(first, second), pageRequest, 2));
        when(imageRepository.findByTargetNameAndPurposeAndTargetNoInOrderByTargetNoAscCreatedAtDesc(
                eq("campaign"),
                eq("REPRESENTATIVE"),
                eq(List.of(10L, 11L))
        )).thenReturn(List.of(
                Image.builder()
                        .targetNo(10L)
                        .imgPath("/images/10.png")
                        .purpose("REPRESENTATIVE")
                        .build(),
                Image.builder()
                        .targetNo(11L)
                        .imgPath("/images/11.png")
                        .purpose("REPRESENTATIVE")
                        .build()
        ));

        CampaignListPageResponseDTO result = campaignService.getCampaignList(1, 6, "deadline", null, null, false);

        assertThat(result.content()).hasSize(2);
        assertThat(result.content().get(0).getCampaignNo()).isEqualTo(10L);
        assertThat(result.content().get(0).getCampaignStatus()).isEqualTo(CampaignStatus.ACTIVE);
        assertThat(result.content().get(0).getStartAt()).isEqualTo(LocalDateTime.of(2026, 4, 1, 0, 0));
        assertThat(result.content().get(0).getEndAt()).isEqualTo(LocalDateTime.of(2026, 4, 10, 0, 0));
        assertThat(result.pageInfo().page()).isEqualTo(1);
        assertThat(result.pageInfo().size()).isEqualTo(6);
        assertThat(result.pageInfo().hasNext()).isFalse();
    }

    @Test
    @DisplayName("includeClosed=true 이면 종료 상태를 포함하고 참여순 정렬로 조회한다")
    void getCampaignList_includeClosedAndParticipationSort() {
        Foundation foundation = Foundation.builder()
                .foundationNo(2L)
                .foundationName("환경 단체")
                .build();

        Campaign highAmount = Campaign.builder()
                .campaignNo(21L)
                .title("강 조성 사업")
                .targetAmount(3_000_000L)
                .currentAmount(BigDecimal.valueOf(2_500_000L))
                .category(CampaignCategory.ENVIRONMENT)
                .foundationNo(2L)
                .foundation(foundation)
                .campaignStatus(CampaignStatus.COMPLETED)
                .build();

        PageRequest pageRequest = PageRequest.of(1, 6, Sort.by(Sort.Order.desc("currentAmount"), Sort.Order.desc("campaignNo")));
        when(campaignRepository.findPublicCampaignPage(
                eq(ApprovalStatus.APPROVED),
                any(),
                eq(CampaignCategory.ENVIRONMENT),
                eq("환경"),
                eq(pageRequest)
        )).thenReturn(new PageImpl<>(List.of(highAmount), pageRequest, 7));
        when(imageRepository.findByTargetNameAndPurposeAndTargetNoInOrderByTargetNoAscCreatedAtDesc(
                eq("campaign"),
                eq("REPRESENTATIVE"),
                eq(List.of(21L))
        )).thenReturn(List.of());

        CampaignListPageResponseDTO result = campaignService.getCampaignList(2, 6, "participation", "환경", "environment", true);

        ArgumentCaptor<List> statusCaptor = ArgumentCaptor.forClass(List.class);
        verify(campaignRepository).findPublicCampaignPage(
                eq(ApprovalStatus.APPROVED),
                statusCaptor.capture(),
                eq(CampaignCategory.ENVIRONMENT),
                eq("환경"),
                eq(pageRequest)
        );

        assertThat(statusCaptor.getValue()).contains(
                CampaignStatus.ACTIVE,
                CampaignStatus.ENDED,
                CampaignStatus.SETTLED,
                CampaignStatus.COMPLETED
        );
        assertThat(result.content()).hasSize(1);
        assertThat(result.content().get(0).getCampaignNo()).isEqualTo(21L);
        assertThat(result.pageInfo().page()).isEqualTo(2);
        assertThat(result.pageInfo().totalElements()).isEqualTo(7);
        assertThat(result.pageInfo().hasNext()).isFalse();
    }
}

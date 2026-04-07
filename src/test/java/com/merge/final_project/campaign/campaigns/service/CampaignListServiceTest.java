package com.merge.final_project.campaign.campaigns.service;

import com.merge.final_project.campaign.campaigns.ApprovalStatus;
import com.merge.final_project.campaign.campaigns.dto.CampaignListResponseDTO;
import com.merge.final_project.campaign.campaigns.entity.Campaign;
import com.merge.final_project.campaign.campaigns.repository.CampaignRepository;
import com.merge.final_project.campaign.useplan.repository.UsePlanRepository;
import com.merge.final_project.global.Image;
import com.merge.final_project.global.ImageRepository;
import com.merge.final_project.global.utils.FileUtil;
import com.merge.final_project.org.foundation.Foundation;
import com.merge.final_project.org.foundation.FoundationRepository;
import com.merge.final_project.wallet.repository.WalletRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
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
    @DisplayName("마감 기준 정렬 시 종료일이 빠른 순으로 정렬되고 최신 대표 이미지가 매핑되는지 테스트")
    void getCampaignList_deadline() {

        // 마감(deadline) 기준 정렬 시 endAt 오름차순으로 되는지
        // 대표 이미지 중 가장 최신(createdAt 기준)이 선택되는지
        // 재단 이름, 이미지 경로가 정상 매핑되는지

        Foundation foundation = Foundation.builder()
                .foundationNo(1L)
                .foundationName("행복 재단")
                .build();

        Campaign first = Campaign.builder()
                .campaignNo(10L)
                .title("긴급 의료 지원")
                .targetAmount(1_000_000L)
                .currentAmount(300_000L)
                .category("의료")
                .endAt(LocalDateTime.of(2026, 4, 10, 0, 0))
                .foundationNo(1L)
                .approvalStatus(ApprovalStatus.APPROVED)
                .build();
        first.setFoundation(foundation);

        Campaign second = Campaign.builder()
                .campaignNo(11L)
                .title("아동 교육 지원")
                .targetAmount(2_000_000L)
                .currentAmount(700_000L)
                .category("교육")
                .endAt(LocalDateTime.of(2026, 4, 12, 0, 0))
                .foundationNo(1L)
                .approvalStatus(ApprovalStatus.APPROVED)
                .build();
        second.setFoundation(foundation);

        when(campaignRepository.findByApprovalStatusOrderByEndAtAscCampaignNoDesc(ApprovalStatus.APPROVED))
                .thenReturn(List.of(first, second));

        when(imageRepository.findByTargetNameAndPurposeAndTargetNoIn(
                eq("campaign"),
                eq("REPRESENTATIVE"),
                eq(List.of(10L, 11L))
        )).thenReturn(List.of(
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
                        .build(),
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

        List<CampaignListResponseDTO> result = campaignService.getCampaignList("마감임박");

        // 결과 검증
        assertThat(result).hasSize(2);
        // 첫 번째 캠페인이 더 빠른 종료일인지 확인
        assertThat(result.get(0).getCampaignNo()).isEqualTo(10L);
        // 최신 이미지가 선택되었는지 확인
        assertThat(result.get(0).getImagePath()).isEqualTo("/images/10-new.png");
        // 재단 이름 매핑 확인
        assertThat(result.get(0).getFoundationName()).isEqualTo("행복 재단");
        // 종료일 정렬 확인
        assertThat(result.get(0).getEndAt()).isEqualTo(LocalDateTime.of(2026, 4, 10, 0, 0));
        assertThat(result.get(1).getCampaignNo()).isEqualTo(11L);

        verify(campaignRepository).findByApprovalStatusOrderByEndAtAscCampaignNoDesc(ApprovalStatus.APPROVED);
    }

    @Test
    @DisplayName("참여율 기준 정렬 시 현재 금액이 높은 순으로 정렬되는지 테스트")
    void getCampaignList_participation() {

        // 참여율(participation) 기준 정렬 시 currentAmount 내림차순인지
        // 대표 이미지가 정상 매핑되는지
        // 재단 이름과 금액이 정확히 전달되는지
        Foundation foundation = Foundation.builder()
                .foundationNo(2L)
                .foundationName("나눔 재단")
                .build();

        Campaign campaign = Campaign.builder()
                .campaignNo(21L)
                .title("생활 지원")
                .targetAmount(3_000_000L)
                .currentAmount(2_500_000L)
                .category("생활")
                .endAt(LocalDateTime.of(2026, 4, 20, 0, 0))
                .foundationNo(2L)
                .approvalStatus(ApprovalStatus.APPROVED)
                .build();
        campaign.setFoundation(foundation);

        when(campaignRepository.findByApprovalStatusOrderByCurrentAmountDescCampaignNoDesc(ApprovalStatus.APPROVED))
                .thenReturn(List.of(campaign));

        when(imageRepository.findByTargetNameAndPurposeAndTargetNoIn(
                eq("campaign"),
                eq("REPRESENTATIVE"),
                eq(List.of(21L))
        )).thenReturn(List.of(
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

        List<CampaignListResponseDTO> result = campaignService.getCampaignList("참여율");

        // 결과 검증
        assertThat(result).hasSize(1);
        // 캠페인 번호 확인
        assertThat(result.get(0).getCampaignNo()).isEqualTo(21L);
        // 현재 금액이 그대로 올바르게 들어왔는지 확인
        assertThat(result.get(0).getCurrentAmount()).isEqualTo(2_500_000L);
        // 이미지 매핑 확인
        assertThat(result.get(0).getImagePath()).isEqualTo("/images/21.png");
        // 재단 이름 확인
        assertThat(result.get(0).getFoundationName()).isEqualTo("나눔 재단");
        verify(campaignRepository).findByApprovalStatusOrderByCurrentAmountDescCampaignNoDesc(ApprovalStatus.APPROVED);
    }
}

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
import com.merge.final_project.wallet.repository.WalletRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CampaignListServiceTest {

    // 가짜(Mock) 객체 생성: 실제 DB에 연결하지 않고 반환값만 설정함
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

    // 위에서 만든 Mock 객체들을 실제 서비스 구현체에 주입함
    @InjectMocks
    private CampaignServiceImpl campaignService;

    @Test
    @DisplayName("마감임박순 조회 시 RECRUITING 상태인 캠페인 목록을 정상적으로 반환한다")
    void getCampaignList_deadline() {
        // 1. Given: 테스트에 필요한 가상 데이터 준비
        Foundation foundation = Foundation.builder()
            .foundationNo(1L)
            .foundationName("희망 재단")
            .build();

        Campaign first = Campaign.builder()
            .campaignNo(10L)
            .title("긴급 구호 캠페인")
            .targetAmount(1_000_000L)
            .currentAmount(BigDecimal.valueOf(300_000L))
            .category("재난")
            .endAt(LocalDateTime.of(2026, 4, 10, 0, 0))
            .foundationNo(1L)
            .campaignStatus(CampaignStatus.RECRUITING)
            .build();
        first.setFoundation(foundation);

        Campaign second = Campaign.builder()
            .campaignNo(11L)
            .title("유기견 보호 캠페인")
            .targetAmount(2_000_000L)
            .currentAmount(BigDecimal.valueOf(700_000L))
            .category("동물")
            .endAt(LocalDateTime.of(2026, 4, 12, 0, 0))
            .foundationNo(1L)
            .campaignStatus(CampaignStatus.RECRUITING)
            .build();
        second.setFoundation(foundation);

        // 2. Mock 설정: 특정 메서드 호출 시 어떤 값을 반환할지 정의
        // 마감임박순으로 정렬된 캠페인 리스트 반환 설정
        when(campaignRepository.findByCampaignStatusOrderByEndAtAscCampaignNoDesc(CampaignStatus.RECRUITING))
            .thenReturn(List.of(first, second));

        // 캠페인 10번에 대한 이미지 리스트 반환 설정 (최신 이미지가 위로 오는지 테스트용)
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

        // 캠페인 11번에 대한 이미지 리스트 반환 설정
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

        // 3. When: 실제 서비스의 목록 조회 기능 실행
        List<CampaignListResponseDTO> result = campaignService.getCampaignList("deadline");

        // 4. Then: 결과 검증 (Assertion)
        assertThat(result).hasSize(2); // 두 개의 캠페인이 반환되었는가
        assertThat(result.get(0).getCampaignNo()).isEqualTo(10L); // 첫 번째가 10번인가
        assertThat(result.get(0).getImagePath()).isEqualTo("/images/10-new.png"); // 최신 이미지가 들어갔는가
        assertThat(result.get(0).getFoundationName()).isEqualTo("희망 재단"); // 재단 이름이 맞는가
        assertThat(result.get(0).getEndAt()).isEqualTo(LocalDateTime.of(2026, 4, 10, 0, 0)); // 종료일이 맞는가
        assertThat(result.get(1).getCampaignNo()).isEqualTo(11L); // 두 번째가 11번인가

        // Repository 메서드가 실제로 호출되었는지 확인
        verify(campaignRepository).findByCampaignStatusOrderByEndAtAscCampaignNoDesc(CampaignStatus.RECRUITING);
    }

    @Test
    @DisplayName("참여도순 조회 시 RECRUITING 상태인 캠페인 중 모금액이 많은 순으로 반환한다")
    void getCampaignList_participation() {
        // 1. Given: 참여도순 테스트를 위한 가상 데이터 준비
        Foundation foundation = Foundation.builder()
            .foundationNo(2L)
            .foundationName("환경 단체")
            .build();

        Campaign campaign = Campaign.builder()
            .campaignNo(21L)
            .title("숲 조성 사업")
            .targetAmount(3_000_000L)
            .currentAmount(BigDecimal.valueOf(2_500_000L))
            .category("환경")
            .endAt(LocalDateTime.of(2026, 4, 20, 0, 0))
            .foundationNo(2L)
            .campaignStatus(CampaignStatus.RECRUITING)
            .build();
        campaign.setFoundation(foundation);

        // 2. Mock 설정: 모금액순(참여도순) 정렬 메서드 호출 시 데이터 반환
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

        // 3. When: 참여도순 정렬 파라미터를 넣어 기능 실행
        List<CampaignListResponseDTO> result = campaignService.getCampaignList("participation");

        // 4. Then: 결과 검증
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCampaignNo()).isEqualTo(21L);
        assertThat(result.get(0).getCurrentAmount()).isEqualTo(2_500_000L);
        assertThat(result.get(0).getImagePath()).isEqualTo("/images/21.png");
        assertThat(result.get(0).getFoundationName()).isEqualTo("환경 단체");

        // 참여도순 Repository 메서드가 호출되었는지 확인
        verify(campaignRepository).findByCampaignStatusOrderByCurrentAmountDescCampaignNoDesc(CampaignStatus.RECRUITING);
    }
}

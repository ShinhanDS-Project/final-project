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

}

package com.merge.final_project.campaign.campaigns.service;

import com.merge.final_project.campaign.campaigns.dto.BeneficiaryResponseDto;
import com.merge.final_project.campaign.campaigns.dto.CampaignRequestDto;

public interface CampaignService {
    // 참여코드로 수혜자 정보 확인
    BeneficiaryResponseDto verifyBeneficiary(Integer entryCode);

    // 캠페인 등록(+ 기부단체 지갑 설정 변경 INACTIVE -> ACTIVE )
    void registerCampaign(CampaignRequestDto requestDto, Long foundationNo);
}

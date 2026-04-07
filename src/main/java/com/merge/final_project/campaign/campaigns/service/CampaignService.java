package com.merge.final_project.campaign.campaigns.service;

import com.merge.final_project.campaign.campaigns.dto.CampaignRequestDto;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

public interface CampaignService {
    // 캠페인 등록 및 기부단체 지갑 상태 변경
    void registerCampaign(CampaignRequestDto requestDto, MultipartFile imageFile, List<MultipartFile> detailImageFiles, Long foundationNo);
}

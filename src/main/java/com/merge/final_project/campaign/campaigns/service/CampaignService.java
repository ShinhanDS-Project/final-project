package com.merge.final_project.campaign.campaigns.service;

import com.merge.final_project.campaign.campaigns.dto.CampaignListResponseDTO;
import com.merge.final_project.campaign.campaigns.dto.CampaignRequestDTO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface CampaignService {
    // 캠페??비즈?�스 로직 ?�의
    void registerCampaign(
            CampaignRequestDTO requestDto,  // 캠페??기본 ?�보
            MultipartFile imageFile,    // ?�???��?지 ?�일 (1??
            List<MultipartFile> detailImageFiles,   // ?�세 ?�이지???��?지 ?�일 리스??(?�러 ??
            Long foundationNo
    );

    // 캠페??목록 조회
    List<CampaignListResponseDTO> getCampaignList(String sort);
}


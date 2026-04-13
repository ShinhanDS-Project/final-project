package com.merge.final_project.campaign.campaigns.service;

import com.merge.final_project.campaign.campaigns.dto.CampaignListResponseDTO;
import com.merge.final_project.campaign.campaigns.dto.CampaignRequestDTO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface CampaignService {
    void registerCampaign(
            CampaignRequestDTO requestDto,
            MultipartFile imageFile,
            List<MultipartFile> detailImageFiles,
            Long foundationNo
    );

    List<CampaignListResponseDTO> getCampaignList(String sort);
}

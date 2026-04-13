package com.merge.final_project.campaign.campaigns.service;

import com.merge.final_project.campaign.campaigns.dto.CampaignBeneficiaryCheckResponseDTO;
import com.merge.final_project.campaign.campaigns.dto.CampaignDetailResponseDTO;
import com.merge.final_project.campaign.campaigns.dto.CampaignFoundationCheckResponseDTO;
import com.merge.final_project.campaign.campaigns.dto.CampaignListResponseDTO;
import com.merge.final_project.campaign.campaigns.dto.CampaignRegisterResponseDTO;
import com.merge.final_project.campaign.campaigns.dto.CampaignRequestDTO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface CampaignService {
    CampaignRegisterResponseDTO registerCampaign(
            CampaignRequestDTO requestDto,
            MultipartFile imageFile,
            List<MultipartFile> detailImageFiles,
            Long foundationNo
    );

    List<CampaignListResponseDTO> getCampaignList(String sort, String searchType, String keyword, String category);

    CampaignDetailResponseDTO getCampaignDetail(Long campaignNo);

    CampaignBeneficiaryCheckResponseDTO checkBeneficiaryByEntryCode(String entryCode);

    CampaignFoundationCheckResponseDTO checkFoundationWalletStatus(Long foundationNo);
}

package com.merge.final_project.campaign.campaigns.service;

import com.merge.final_project.campaign.campaigns.dto.CampaignBeneficiaryCheckResponseDTO;
import com.merge.final_project.campaign.campaigns.dto.CampaignDetailResponseDTO;
import com.merge.final_project.campaign.campaigns.dto.CampaignFoundationCheckResponseDTO;
import com.merge.final_project.campaign.campaigns.dto.CampaignListPageResponseDTO;
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

    CampaignRegisterResponseDTO updatePendingCampaign(
            Long campaignNo,
            CampaignRequestDTO requestDto,
            MultipartFile imageFile,
            List<MultipartFile> detailImageFiles,
            Long foundationNo
    );

    CampaignListPageResponseDTO getCampaignList(
            int page,
            int size,
            String sort,
            String keyword,
            String category,
            boolean includeClosed
    );

    CampaignDetailResponseDTO getCampaignDetail(Long campaignNo);

    CampaignDetailResponseDTO getPendingCampaignForEdit(Long campaignNo, Long foundationNo);

    CampaignBeneficiaryCheckResponseDTO checkBeneficiaryByEntryCode(String entryCode);

    CampaignFoundationCheckResponseDTO checkFoundationWalletStatus(Long foundationNo);
}

package com.merge.final_project.admin.service;

import com.merge.final_project.campaign.campaigns.dto.CampaignListResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AdminCampaignService {

    void approveCampaign(Long campaignNo);

    void rejectCampaign(Long campaignNo, String reason);

    Page<CampaignListResponseDTO> getPendingCampaigns(Pageable pageable);

    Page<CampaignListResponseDTO> getRejectedCampaigns(Pageable pageable);

    Page<CampaignListResponseDTO> getApprovedCampaigns(Pageable pageable);
}
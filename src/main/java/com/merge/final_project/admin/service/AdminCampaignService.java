package com.merge.final_project.admin.service;

import com.merge.final_project.campaign.campaigns.dto.CampaignListResponseDto;
import com.merge.final_project.campaign.campaigns.entity.Campaign;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AdminCampaignService {

    void approveCampaign(Long campaignNo);

    void rejectCampaign(Long campaignNo, String reason);

    Page<CampaignListResponseDto> getPendingCampaigns(Pageable pageable);

    Page<CampaignListResponseDto> getRejectedCampaigns(Pageable pageable);

    Page<CampaignListResponseDto> getApprovedCampaigns(Pageable pageable);
}

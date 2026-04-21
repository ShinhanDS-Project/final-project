package com.merge.final_project.campaign.campaigns.dto;

import lombok.Builder;

import java.util.List;

@Builder
public record CampaignListPageResponseDTO(
        List<CampaignListResponseDTO> content,
        CampaignListPageInfoDTO pageInfo
) {
}

package com.merge.final_project.campaign.campaigns.dto;

import lombok.Builder;

@Builder
public record CampaignListPageInfoDTO(
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean hasNext,
        boolean last
) {
}

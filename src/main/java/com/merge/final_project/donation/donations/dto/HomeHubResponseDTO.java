package com.merge.final_project.donation.donations.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HomeHubResponseDTO {
    private long totalDonationCount;
    private long totalUserCount;
    private long totalCampaignCount;
    private List<HomeCampaignItemDTO> endingSoon;
    private List<HomeCampaignItemDTO> topParticipation;
    private List<HomeLatestCampaignResponseDTO> latestOngoing;
}

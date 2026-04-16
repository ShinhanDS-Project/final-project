package com.merge.final_project.donation.donations.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
//[바다] - main에서 현재 누적 기부 정보
public class PublicStatsResponseDTO {
    private long totalDonationCount;
    private long totalUserCount;
    private long totalCampaignCount;
}

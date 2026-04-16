package com.merge.final_project.donation.donations;

import com.merge.final_project.donation.donations.dto.HomeHubResponseDTO;
import com.merge.final_project.donation.donations.dto.HomeLatestCampaignResponseDTO;
import com.merge.final_project.donation.donations.dto.PublicStatsResponseDTO;
import com.merge.final_project.donation.donations.dto.RecentDonationFeedItemDTO;

import java.util.List;

public interface DonationService {
    List<Donation> requestDonation(Long userNo);

    //[바다] main 사용 로직
    PublicStatsResponseDTO getPublicStats();

    HomeHubResponseDTO getHomeHub();
    //[바다] 기부 최근 5개
    List<RecentDonationFeedItemDTO> getRecentPublicDonations(int limit);

    //[바다] 메인 캠페인 리스트
    List<HomeLatestCampaignResponseDTO> getLatestOngoingCampaigns(int limit);
}

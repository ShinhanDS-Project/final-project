package com.merge.final_project.donation.donations;

import com.merge.final_project.donation.donations.dto.HomeHubResponseDTO;
import com.merge.final_project.donation.donations.dto.PublicStatsResponseDTO;

import java.util.List;

public interface DonationService {
    List<Donation> requestDonation(Long userNo);

    //[바다] main 사용 로직
    PublicStatsResponseDTO getPublicStats();

    HomeHubResponseDTO getHomeHub();
}
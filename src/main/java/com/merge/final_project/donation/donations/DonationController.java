package com.merge.final_project.donation.donations;

import com.merge.final_project.donation.donations.dto.HomeHubResponseDTO;
import com.merge.final_project.donation.donations.dto.HomeLatestCampaignResponseDTO;
import com.merge.final_project.donation.donations.dto.PublicStatsResponseDTO;
import com.merge.final_project.donation.donations.dto.RecentDonationFeedItemDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
@RestController
@RequestMapping("/api/donation")
@RequiredArgsConstructor
public class DonationController {


    private final DonationService donationService;
    //기부내역 조회
    @GetMapping("/see/mydonation")
    public ResponseEntity<List<Donation>> requestDonationFind(Authentication authentication){
        Long loginUserNo = (Long) authentication.getDetails();
        List<Donation> donations=donationService.requestDonation(loginUserNo);
        return ResponseEntity.ok(donations);
    }

    //[바다] main 기부 누적 내역 조회
    @GetMapping("/public/stats")
    public ResponseEntity<PublicStatsResponseDTO> getPublicStats() {
        return ResponseEntity.ok(donationService.getPublicStats());
    }

    //[바다] main 캠페인 리스트
    @GetMapping("/public/home-hub")
    public ResponseEntity<HomeHubResponseDTO> getHomeHub() {
        return ResponseEntity.ok(donationService.getHomeHub());
    }

    //[바다] 기부 최근 5개
    @GetMapping("/public/recent-donations")
    public ResponseEntity<List<RecentDonationFeedItemDTO>> getRecentPublicDonations(
            @RequestParam(defaultValue = "5") int limit
    ) {
        return ResponseEntity.ok(donationService.getRecentPublicDonations(limit));
    }

    //[바다] main 캠페인 기부 리스트
    @GetMapping("/public/latest-campaigns")
    public ResponseEntity<List<HomeLatestCampaignResponseDTO>> getLatestOngoingCampaigns(
            @RequestParam(defaultValue = "5") int limit
    ) {
        return ResponseEntity.ok(donationService.getLatestOngoingCampaigns(limit));
    }


}

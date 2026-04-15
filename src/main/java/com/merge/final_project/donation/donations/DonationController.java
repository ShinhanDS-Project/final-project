package com.merge.final_project.donation.donations;

import com.merge.final_project.donation.donations.dto.HomeHubResponseDTO;
import com.merge.final_project.donation.donations.dto.PublicStatsResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
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


}

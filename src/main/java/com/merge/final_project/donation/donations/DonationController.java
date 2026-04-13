package com.merge.final_project.donation.donations;

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
}

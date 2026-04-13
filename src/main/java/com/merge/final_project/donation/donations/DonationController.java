package com.merge.final_project.donation.donations;

import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;
@RequiredArgsConstructor
public class DonationController {


    private final DonationService donationService;
    //기부내역 조회
    @GetMapping("/see/donation")
    public ResponseEntity<List<Donation>> requestDonationFind(Long userNo){
        List<Donation> donations=donationService.requestDonation(userNo);
        return ResponseEntity.ok(donations);
    }
}

package com.merge.final_project.donation.donations;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
@io.swagger.v3.oas.annotations.tags.Tag(name = "기부 내역", description = "사용자 기부 내역 조회 API")
@RestController
@RequestMapping("/api/donation")
@RequiredArgsConstructor
public class DonationController {


    private final DonationService donationService;
    @Operation(summary = "내 기부 내역 조회", description = "로그인한 사용자의 기부 내역 목록을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 요청")
    })
    @GetMapping("/see/mydonation")
    public ResponseEntity<List<Donation>> requestDonationFind(Authentication authentication){
        Long loginUserNo = (Long) authentication.getDetails();
        List<Donation> donations=donationService.requestDonation(loginUserNo);
        return ResponseEntity.ok(donations);
    }
}

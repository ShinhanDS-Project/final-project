package com.merge.final_project.blockchain.controller;

import com.merge.final_project.blockchain.dto.BlockchainTransferResponse;
import com.merge.final_project.blockchain.dto.DonationTokenTransferRequest;
import com.merge.final_project.blockchain.dto.PaymentTokenChargeRequest;
import com.merge.final_project.blockchain.service.BlockchainTransferService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/blockchain")
@RequiredArgsConstructor
public class BlockchainTransferController {

    private final BlockchainTransferService blockchainTransferService;

    @PostMapping("/payments/complete")
    public ResponseEntity<BlockchainTransferResponse> completePayment(@RequestBody PaymentTokenChargeRequest request) {
        return ResponseEntity.ok(
                blockchainTransferService.chargeUserToken(
                        request.userNo(),
                        request.amount(),
                        request.donationId()
                )
        );
    }

    @PostMapping("/donations")
    public ResponseEntity<BlockchainTransferResponse> transferDonation(@RequestBody DonationTokenTransferRequest request) {
        return ResponseEntity.ok(
                blockchainTransferService.transferDonationToCampaign(
                        request.userNo(),
                        request.campaignNo(),
                        request.amount(),
                        request.donationId()
                )
        );
    }
}

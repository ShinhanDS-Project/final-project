package com.merge.final_project.blockchain.controller;

import com.merge.final_project.blockchain.dto.BlockchainTransferResponse;
import com.merge.final_project.blockchain.dto.DonationTokenTransferRequest;
import com.merge.final_project.blockchain.dto.PaymentTokenChargeRequest;
import com.merge.final_project.blockchain.service.BlockchainService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/blockchain")
@RequiredArgsConstructor
public class BlockchainController {

    private final BlockchainService blockchainService;

    /**
     * 결제 완료 후 SERVER(HOT) -> USER 토큰 지급을 실행한다.
     */
    @PostMapping("/payments/complete")
    public ResponseEntity<BlockchainTransferResponse> completePayment(@RequestBody PaymentTokenChargeRequest request) {
        return ResponseEntity.ok(blockchainService.chargeUserToken(request.userNo(), request.amount(), request.donationId()));
    }

    /**
     * 기부 요청 시 USER -> CAMPAIGN 토큰 전송을 실행한다.
     */
    @PostMapping("/donations")
    public ResponseEntity<BlockchainTransferResponse> transferDonation(@RequestBody DonationTokenTransferRequest request) {
        return ResponseEntity.ok(
                blockchainService.transferDonationToCampaign(
                        request.userNo(),
                        request.campaignNo(),
                        request.amount(),
                        request.donationId()
                )
        );
    }
}

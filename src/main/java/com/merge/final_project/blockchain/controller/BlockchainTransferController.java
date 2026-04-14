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

@io.swagger.v3.oas.annotations.tags.Tag(name = "블록체인 전송", description = "토큰 전송·결제 충전 API")
@RestController
@RequestMapping("/blockchain")
@RequiredArgsConstructor
public class BlockchainTransferController {

    private final BlockchainTransferService blockchainTransferService;

    /**
     * 결제 승인 직후 호출되는 토큰 충전 API.
     * 서버(소유자) 측 토큰을 기부자 지갑으로 배정하고 거래내역을 저장한다.
     */
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

    /**
     * 기부 확정 시 호출되는 토큰 이체 API.
     * 기부자 지갑에서 캠페인 지갑으로 토큰을 이동하고 거래내역을 저장한다.
     */
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

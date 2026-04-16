package com.merge.final_project.blockchain.controller;

import com.merge.final_project.blockchain.dto.BlockchainTransferResponse;
import com.merge.final_project.blockchain.dto.DonationTokenTransferRequest;
import com.merge.final_project.blockchain.dto.PaymentTokenChargeRequest;
import com.merge.final_project.blockchain.service.BlockchainTransferService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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

    @Operation(summary = "결제 토큰 충전", description = "결제 승인 직후 서버 HOT 지갑에서 기부자 지갑으로 토큰을 충전합니다. 거래 내역이 저장됩니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "충전 성공"),
            @ApiResponse(responseCode = "400", description = "요청 값 오류")
    })
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

    @Operation(summary = "기부 토큰 이체", description = "기부 확정 시 기부자 지갑에서 캠페인 지갑으로 토큰을 이체합니다. 거래 내역이 저장됩니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "이체 성공"),
            @ApiResponse(responseCode = "400", description = "요청 값 오류")
    })
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

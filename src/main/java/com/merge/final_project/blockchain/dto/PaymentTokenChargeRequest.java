package com.merge.final_project.blockchain.dto;

/**
 * 결제 완료 후 토큰 지급 요청 payload.
 */
public record PaymentTokenChargeRequest(
        Integer userNo,
        Long amount,
        Long donationId
) {
}

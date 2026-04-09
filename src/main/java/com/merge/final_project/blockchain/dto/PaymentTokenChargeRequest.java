package com.merge.final_project.blockchain.dto;

/**
 * 결제 승인 후 토큰 충전 요청 DTO.
 */
public record PaymentTokenChargeRequest(
        // 기부자 user_no
        Long userNo,
        // 충전할 금액(도메인 단위)
        Long amount,
        // 기부 식별자(없으면 서비스에서 fallback 생성)
        Long donationId
) {
}

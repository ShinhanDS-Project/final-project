package com.merge.final_project.blockchain.dto;

/**
 * 기부자 -> 캠페인 토큰 이체 요청 DTO.
 */
public record DonationTokenTransferRequest(
        // 기부자 user_no
        Long userNo,
        // 캠페인 campaign_no
        Long campaignNo,
        // 이체 금액(도메인 단위)
        Long amount,
        // 기부 식별자(없으면 서비스에서 fallback 생성)
        Long donationId
) {
}

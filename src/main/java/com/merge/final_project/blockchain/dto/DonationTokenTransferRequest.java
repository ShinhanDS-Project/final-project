package com.merge.final_project.blockchain.dto;

/**
 * USER -> CAMPAIGN 기부 전송 요청 payload.
 */
public record DonationTokenTransferRequest(
        Integer userNo,
        Long campaignNo,
        Long amount,
        Long donationId
) {
}

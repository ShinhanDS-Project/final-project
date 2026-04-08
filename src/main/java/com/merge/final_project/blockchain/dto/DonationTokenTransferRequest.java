package com.merge.final_project.blockchain.dto;

public record DonationTokenTransferRequest(
        Long userNo,
        Long campaignNo,
        Long amount,
        Long donationId
) {
}

package com.merge.final_project.blockchain.dto;

public record PaymentTokenChargeRequest(
        Long userNo,
        Long amount,
        Long donationId
) {
}

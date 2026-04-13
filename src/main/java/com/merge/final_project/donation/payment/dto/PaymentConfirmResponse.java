package com.merge.final_project.donation.payment.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class PaymentConfirmResponse {

    private Long paymentNo;
    private Long donationNo;
    private String orderId;
    private String paymentKey;
    private BigDecimal amount;
    private String status;
    private String message;
}
package com.merge.final_project.donation.payment.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class PaymentReadyResponse {

    private Long paymentNo;
    private String orderId;
    private BigDecimal amount;
    private String orderName;
}
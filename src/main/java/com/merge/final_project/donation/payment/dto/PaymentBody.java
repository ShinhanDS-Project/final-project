package com.merge.final_project.donation.payment.dto;

import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;

@Getter
public class PaymentBody {
    private String paymentKey;
    private String orderId;
    private String status; // 상태값들
    private String method; // method- card, pay
    private BigDecimal amount;    // 실제로 결제된 총 금액
    private LocalDateTime requestedAt;  // 결제가 요청된 시각
    private OffsetDateTime approvedAt; //승인시간
}

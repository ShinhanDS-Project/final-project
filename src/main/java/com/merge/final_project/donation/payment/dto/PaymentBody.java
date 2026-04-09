package com.merge.final_project.donation.payment.dto;

import com.merge.final_project.donation.payment.PaymentMethod;
import com.merge.final_project.donation.payment.PaymentStatus;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class PaymentBody {
    private String paymentKey;
    private String orderId;
    private PaymentStatus paymentStatus; // 상태값들
    private PaymentMethod paymentMethod; // method- card, pay
    private BigDecimal totalAmount;    // 실제로 결제된 총 금액
    private String requestedAt;  // 결제가 요청된 시각
    private String approvedAt; //승인시간
}

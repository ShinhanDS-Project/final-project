package com.merge.final_project.donation.payment.dto;

import com.merge.final_project.donation.payment.PaymentStatus;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
@Getter
@Setter
@Builder
public class PaymentByUserResponse {
    //상태
    private PaymentStatus paymentStatus;
    //가격
    private BigDecimal amount;

    private Long userNo;
    private Long campaignNo;

    private String orderKey;
    private LocalDateTime paidAt;

    private String paymentMethod;


}

package com.merge.final_project.donation.payment.dto;

import com.merge.final_project.donation.payment.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PaymentByUserResponse {
    //상태
    PaymentStatus paymentStatus;
    //가격
    BigDecimal amount;

    Long userNo;
    Long campaignNo;

    String orderKey;
    LocalDateTime paidAt;

    String paymentMethod;


}

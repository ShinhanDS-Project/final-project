package com.merge.final_project.donation.payment.dto;

import com.merge.final_project.donation.payment.PaymentMethod;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class PaymentConfirmRequest {
    @NotNull(message="오류가 발생하였습니다.")
    private String paymentKey;

    @NotNull(message="오류가 발생하였습니다.")
    private String orderId;

    @NotNull(message="오류가 발생하였습니다.")
    private BigDecimal amount;

    @NotNull(message="오류가 발생하였습니다.")
    private PaymentMethod method;


}

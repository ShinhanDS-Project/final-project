package com.merge.final_project.donation.payment.dto;

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


}

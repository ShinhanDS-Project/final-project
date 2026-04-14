package com.merge.final_project.donation.payment.dto;

import com.merge.final_project.donation.payment.PaymentMethod;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class PaymentConfirmRequest {
    @NotBlank(message="오류가 발생하였습니다.")
    private String paymentKey;

    @NotBlank(message="오류가 발생하였습니다.")
    private String orderId;

    @DecimalMin(value = "0.01", inclusive = true, message="오류가 발생하였습니다.")
    private BigDecimal amount;

    @NotBlank(message="오류가 발생하였습니다.")
    private PaymentMethod method;



}

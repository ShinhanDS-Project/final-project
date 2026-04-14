package com.merge.final_project.donation.payment.dto;

import com.merge.final_project.donation.payment.PaymentMethod;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentReadyRequest {

    @NotNull(message = "오류 발생하였습니다.")
    private Long campaignNo;

    @NotNull(message = "기부 금액은 필수야.")
    @DecimalMin(value = "100.0", inclusive = true, message = "기부 금액은 최소 100원 이상이어야 합니다..")
    private BigDecimal amount;

    @NotNull(message = "익명 여부는 필수입니다.")
    private Boolean isAnonymous;

    @NotNull(message = "결제수단은 필수입니다.")
    private PaymentMethod method;


}
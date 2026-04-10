package com.merge.final_project.org.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
public class FoundationUpdateRequestDTO {

    @NotBlank
    private String description;

    @NotBlank
    private String contactPhone;

    @NotBlank
    private String account;

    @NotBlank
    private String bankName;

    @NotNull
    @DecimalMin("0.00")
    @DecimalMax("1.00")
    private BigDecimal feeRate;
}

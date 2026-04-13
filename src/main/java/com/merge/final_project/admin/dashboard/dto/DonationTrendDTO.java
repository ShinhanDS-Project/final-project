package com.merge.final_project.admin.dashboard.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class DonationTrendDTO {

    private String date;            // "yyyy-MM-dd"
    private BigDecimal amount;
}

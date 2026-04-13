package com.merge.final_project.admin.dashboard.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class CategoryRatioDTO {

    private String category;        // enum name
    private String categoryLabel;   // 한글 표시명
    private long campaignCount;
    private BigDecimal donationAmount;
}

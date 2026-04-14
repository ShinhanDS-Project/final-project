package com.merge.final_project.org.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class FoundationWalletDTO {

    private String walletAddress;   // null이면 지갑 미연동
    private BigDecimal balance;
}

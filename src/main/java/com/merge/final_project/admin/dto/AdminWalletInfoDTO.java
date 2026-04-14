package com.merge.final_project.admin.dto;

import com.merge.final_project.wallet.entity.Wallet;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class AdminWalletInfoDTO {

    private String walletAddress;
    private BigDecimal balance;

    public static AdminWalletInfoDTO from(Wallet wallet) {
        return AdminWalletInfoDTO.builder()
                .walletAddress(wallet.getWalletAddress())
                .balance(wallet.getBalance())
                .build();
    }
}

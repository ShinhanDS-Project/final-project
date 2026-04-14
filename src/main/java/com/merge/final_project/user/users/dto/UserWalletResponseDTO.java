package com.merge.final_project.user.users.dto;

import com.merge.final_project.wallet.entity.WalletStatus;
import com.merge.final_project.wallet.entity.WalletType;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
@Builder
@Getter
public class UserWalletResponseDTO {
    private Long walletNo;
    private String walletAddress;
    private WalletStatus walletStatus;
    private WalletType walletType;
    private Long ownerNo;
    private BigDecimal balance;
    private String walletHash;
    //토큰 거래 확인하기

}

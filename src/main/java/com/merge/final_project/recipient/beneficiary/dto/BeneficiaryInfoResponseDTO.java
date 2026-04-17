package com.merge.final_project.recipient.beneficiary.dto;

import com.merge.final_project.recipient.beneficiary.BeneficiaryType;
import com.merge.final_project.recipient.beneficiary.entity.Beneficiary;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class BeneficiaryInfoResponseDTO {
    private String name;
    private String phone;
    private String account;
    private Long walletNo;
    private String walletAddress; // 💡 지갑 주소 추가
    private java.math.BigDecimal balance; // 💡 잔액 추가
    private String entryCode;
    private BeneficiaryType beneficiaryType;

    public BeneficiaryInfoResponseDTO(Beneficiary entity) {
        this.name = entity.getName();
        this.phone = entity.getPhone();
        this.account = entity.getAccount();
        if (entity.getWallet() != null) {
            this.walletNo = entity.getWallet().getWalletNo();
            this.walletAddress = entity.getWallet().getWalletAddress();
            this.balance = entity.getWallet().getBalance();
        }
        this.entryCode = entity.getEntryCode();
        this.beneficiaryType = entity.getBeneficiaryType();
    }
}

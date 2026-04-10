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
    private String entryCode;
    private BeneficiaryType beneficiaryType;

    public BeneficiaryInfoResponseDTO(Beneficiary entity) {
        this.name = entity.getName();
        this.phone = entity.getPhone();
        this.account = entity.getAccount();
        this.entryCode = entity.getEntryCode();
        this.beneficiaryType = entity.getBeneficiaryType();
    }
}

package com.merge.final_project.recipient.beneficiary.dto;


import com.merge.final_project.recipient.beneficiary.BeneficiaryType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BeneficiarySignupRequestDTO {
    private String email;
    private String password;
    private String name;
    private String phone;
    private String account;
    private BeneficiaryType beneficiaryType;
}

package com.merge.final_project.recipient.beneficiary.dto;

import com.merge.final_project.recipient.beneficiary.BeneficiaryType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BeneficiaryUpdateRequestDTO {
    private String name;
    private String phone;
    private String account;
    private BeneficiaryType beneficiaryType;
    private String password; // 비밀번호 변경 시 사용 (선택)
}

package com.merge.final_project.campaign.campaigns.dto;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BeneficiaryResponseDto {
    private Long beneficiaryNo;
    private String name;
    private String phone;
}

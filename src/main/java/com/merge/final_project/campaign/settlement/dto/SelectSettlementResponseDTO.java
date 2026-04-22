package com.merge.final_project.campaign.settlement.dto;

import com.merge.final_project.campaign.settlement.SettlementStatus;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Builder
@Getter
@Setter
public class SelectSettlementResponseDTO {
    //정산 내용 조회
    //1. 기부단체 pk
    private Long foundationNo;
    //2.정산일시
    private LocalDateTime settledAt;
    //3. 기부단체 수령액 (수수료 계산된 것)
    private Long foundationAmount;
    //4. 수혜자 수령액( 수혜자 수령액= 전체 금액- 기부단체 수령액)
    private Long beneficiaryAmount;
    //5.수혜자 pk(아마 따로 빼와야할듯함)
    private Long beneficiaryNo;
    //6. 정산 상태
    private SettlementStatus settlementStatus;

}

package com.merge.final_project.campaign.settlement.dto;

import com.merge.final_project.campaign.settlement.SettlementStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
@Getter
@Builder
public class SelectSettlementResponseDTO {
    //정산 내용 조회
    //1. 기부단체 pk
    Long foundationNo;
    //2.정산일시
    LocalDateTime settledAt;
    //3. 기부단체 수령액 (수수료 계산된 것)
    Long foundationAmount;
    //4. 수혜자 수령액( 수혜자 수령액= 전체 금액- 기부단체 수령액)
    Long beneficiaryAmount;
    //5.수혜자 pk(아마 따로 빼와야할듯함)
    Long beneficiaryNo;
    //6. 정산 상태
    SettlementStatus settlementStatus;

}

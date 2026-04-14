package com.merge.final_project.org.dto;

import com.merge.final_project.campaign.settlement.Settlement;
import com.merge.final_project.campaign.settlement.SettlementStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class FoundationSettlementDTO {

    private Long settlementNo;
    private String campaignTitle;
    private Long totalAmount;       // 전체 정산 금액
    private Long foundationAmount;  // 기부단체 배정액
    private SettlementStatus status;
    private LocalDateTime settledAt;

    public static FoundationSettlementDTO from(Settlement settlement) {
        return FoundationSettlementDTO.builder()
                .settlementNo(settlement.getSettlementNo())
                .campaignTitle(settlement.getCampaign().getTitle())
                .totalAmount(settlement.getAmount())
                .foundationAmount(settlement.getFoundationAmount())
                .status(settlement.getStatus())
                .settledAt(settlement.getSettledAt())
                .build();
    }
}

package com.merge.final_project.org.dto;

import com.merge.final_project.campaign.settlement.Settlement;
import com.merge.final_project.campaign.settlement.SettlementStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Schema(description = "기부단체 정산 내역 DTO")
@Getter
@Builder
public class FoundationSettlementDTO {

    @Schema(description = "정산 번호", example = "1")
    private Long settlementNo;

    @Schema(description = "캠페인 제목", example = "어린이 급식 지원 캠페인")
    private String campaignTitle;

    @Schema(description = "전체 정산 금액 (원, 수수료 차감 전)", example = "5000000")
    private Long totalAmount;

    @Schema(description = "기부단체 배정액 (원, 수수료 차감 후)", example = "4750000")
    private Long foundationAmount;

    @Schema(description = "정산 상태", example = "COMPLETED")
    private SettlementStatus status;

    @Schema(description = "정산 처리 일시", example = "2024-04-01T00:00:00")
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

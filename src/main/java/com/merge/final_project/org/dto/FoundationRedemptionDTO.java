package com.merge.final_project.org.dto;

import com.merge.final_project.redemption.RedemptionStatus;
import com.merge.final_project.redemption.entity.Redemption;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class FoundationRedemptionDTO {

    private Long redemptionNo;
    private Long amount;
    private RedemptionStatus status;
    private LocalDateTime requestedAt;
    private LocalDateTime processedAt;
    private LocalDateTime cashPaidAt;
    private String failureReason;

    public static FoundationRedemptionDTO from(Redemption redemption) {
        return FoundationRedemptionDTO.builder()
                .redemptionNo(redemption.getRedemptionNo())
                .amount(redemption.getAmount())
                .status(redemption.getStatus())
                .requestedAt(redemption.getRequestedAt())
                .processedAt(redemption.getProcessedAt())
                .cashPaidAt(redemption.getCashPaidAt())
                .failureReason(redemption.getFailureReason())
                .build();
    }
}

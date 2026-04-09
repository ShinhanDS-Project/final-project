package com.merge.final_project.redemption.dto.response;

import com.merge.final_project.redemption.RedemptionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class RedemptionResponse {

    private Long redemptionNo;
    private String requesterType;
    private Long amount;
    private RedemptionStatus status;
    private LocalDateTime requestedAt;
    private LocalDateTime processedAt;
    private Long requesterNo;
    private String failureReason;
}

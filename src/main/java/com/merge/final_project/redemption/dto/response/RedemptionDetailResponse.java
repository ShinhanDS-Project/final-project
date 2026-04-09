package com.merge.final_project.redemption.dto.response;

import com.merge.final_project.redemption.RedemptionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class RedemptionDetailResponse {

    private Long redemptionNo;
    private String requesterName;
    private String requesterType;
    private Long requesterNo;
    private String account;
    private String walletAddress;
    private Long amount;
    private RedemptionStatus status;
    private LocalDateTime requestedAt;
    private LocalDateTime processedAt;
    private LocalDateTime cashPaidAt;
    private Long blockNumber;
    private Long transactionNo;
    private String txHash;
    private String failureReason;
}

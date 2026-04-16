package com.merge.final_project.user.users.dto;

import com.merge.final_project.blockchain.entity.Transaction;
import com.merge.final_project.campaign.campaigns.ApprovalStatus;
import com.merge.final_project.campaign.campaigns.CampaignStatus;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
public class UserTransactionResponseDTO {
    Transaction transaction;
    private Long campaignNo;
    private Long userNo;
    private String title;
    private BigDecimal amount;
    private ApprovalStatus approvalStatus;
    private Long transactionNum; //기부내역
    private BigDecimal total_amount;
    private CampaignStatus campaignStatus;
    private LocalDateTime usageStartAt;
    private LocalDateTime usageEndAt;
}

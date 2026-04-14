package com.merge.final_project.user.users.dto;

import com.merge.final_project.blockchain.entity.Transaction;
import com.merge.final_project.campaign.campaigns.ApprovalStatus;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserTransactionResponseDTO {
    Transaction transaction;
    private Long campaignNo;
    private Long userNo;
    private String title;
    private ApprovalStatus approvalStatus;

}

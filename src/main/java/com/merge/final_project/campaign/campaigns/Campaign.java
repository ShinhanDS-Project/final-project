package com.merge.final_project.campaign.campaigns;

import com.merge.final_project.global.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "campaign")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Campaign extends BaseEntity {

    @Id
    @Column(name = "campaign_no")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long campaignNo;

    private String title;
    private String description;

    @Column(name = "image_path")
    private String imagePath;

    @Column(name = "start_at")
    private LocalDateTime startAt;

    @Column(name = "end_at")
    private LocalDateTime endAt;

    @Column(name = "usage_start_at")
    private LocalDateTime usageStartAt;

    @Column(name = "usage_end_at")
    private LocalDateTime usageEndAt;

    @Column(name = "target_amount")
    private String targetAmount;

    @Column(name = "current_amount")
    private String currentAmount;

    @Column(name = "achived_at")
    private LocalDateTime achivedAt;

    private String disbursement;
    private String category;

    @Enumerated(EnumType.STRING)
    @Column(name = "approval_status")
    private ApprovalStatus approvalStatus;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "campaign_status")
    private CampaignStatus campaignStatus;

    @Column(name = "camp_wallet_no")
    private Long campWalletNo;

    @Column(name = "wallet_no2")
    private Long walletNo2;

    @Column(name = "foundation_no")
    private Long foundationNo;

    @Column(name = "beneficiary_no")
    private Long beneficiaryNo;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "reject_reason")
    private String rejectReason;
}

package com.merge.final_project.campaign.campaigns;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "campaign")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Campaign {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "campaign_no")
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
    private Integer targetAmount;

    @Column(name = "current_amount")
    private Integer currentAmount;

    @Column(name = "achieved_at")
    private LocalDateTime achievedAt;

    private String category;

    @Enumerated(EnumType.STRING)
    @Column(name = "approval_status")
    private ApprovalStatus approvalStatus;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "campaign_status")
    private CampaignStatus campaignStatus;

    @Column(name = "wallet_no")
    private Long walletNo;

    @Column(name = "foundation_no")
    private Long foundationNo;

    @Column(name = "beneficiary_no")
    private Long beneficiaryNo;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "reject_reason")
    private String rejectReason;
}

package com.merge.final_project.campaign.campaigns;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "campaign")
@Getter
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

    // 🔥 전부 String으로 맞춤
    @Column(name = "start_at")
    private String startAt;

    @Column(name = "end_at")
    private String endAt;

    @Column(name = "usage_start_at")
    private String usageStartAt;

    @Column(name = "usage_end_at")
    private String usageEndAt;

    @Column(name = "target_amount")
    private String targetAmount;

    @Column(name = "current_amount")
    private String currentAmount;

    @Column(name = "achieved_at")
    private String achievedAt;

    private String category;

    @Enumerated(EnumType.STRING)
    @Column(name = "approval_status")
    private ApprovalStatus approvalStatus;

    @Column(name = "approved_at")
    private String approvedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "campaign_status")
    private CampaignStatus campaignStatus;

    // 🔥 이것도 varchar라 String
    @Column(name = "wallet_no")
    private String walletNo;

    @Column(name = "foundation_no")
    private String foundationNo;

    @Column(name = "beneficiary_no")
    private String beneficiaryNo;

    @Column(name = "created_at")
    private String createdAt;

    @Column(name = "updated_at")
    private String updatedAt;

    @Column(name = "reject_reason")
    private String rejectReason;
}

package com.merge.final_project.db.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity(name = "DbCampaign")
@Table(name = "campaign")
@Getter
@Setter
@NoArgsConstructor
public class Campaign {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "campaign_no")
    private Integer campaignNo;

    @Column(name = "title")
    private String title;

    @Column(name = "description")
    private String description;

    @Column(name = "image_path")
    private String imagePath;

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

    @Column(name = "achieved_at")
    private String achievedAt;

    @Column(name = "current_amount")
    private String currentAmount;

    @Column(name = "category")
    private String category;

    @Column(name = "approval_status")
    private String approvalStatus;

    @Column(name = "approved_at")
    private String approvedAt;

    @Column(name = "campaign_status")
    private String campaignStatus;

    @Column(name = "beneficiary_no")
    private String beneficiaryNo;

    @Column(name = "created_at", nullable = false)
    private String createdAt;

    @Column(name = "updated_at")
    private String updatedAt;

    @Column(name = "reject_reason")
    private String rejectReason;

    @Column(name = "foundation_no", nullable = false)
    private String foundationNo;

    @Column(name = "wallet_no")
    private String walletNo;
}

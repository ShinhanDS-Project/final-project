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
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "campaign_no")
    private Integer campaignNo;

    private String title;

    @Column(columnDefinition = "TEXT")
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

    @Column(name = "achieved_at")
    private LocalDateTime achievedAt;

    @Column(name = "current_amount")
    private Integer currentAmount;

    private String category;

    @Enumerated(EnumType.STRING)
    @Column(name = "approval_status")
    private ApprovalStatus approvalStatus;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "campaign_status")
    private CampaignStatus campaignStatus;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "reject_reason", columnDefinition = "TEXT")
    private String rejectReason;

    @Column(name = "beneficiary_no")
    private Integer beneficiaryNo;

    @Column(name = "foundation_no")
    private Integer foundationNo;
}

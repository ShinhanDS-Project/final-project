package com.merge.final_project.campaign.campaigns.entity;

import com.merge.final_project.campaign.campaigns.ApprovalStatus;
import com.merge.final_project.campaign.campaigns.CampaignStatus;
import com.merge.final_project.global.BaseCreatedAtEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "campaign")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Campaign extends BaseCreatedAtEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long campaignNo;

    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String imagePath;

    private LocalDateTime startAt;
    private LocalDateTime endAt;
    private LocalDateTime usageStartAt;
    private LocalDateTime usageEndAt;

    private Long targetAmount;
    private Long currentAmount;

    private LocalDateTime achievedAt;
    private String category;

    @Enumerated(EnumType.STRING)
    private ApprovalStatus approvalStatus;

    private LocalDateTime approvedAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Enumerated(EnumType.STRING)
    private CampaignStatus campaignStatus;

    private String rejectReason;

    private Long foundationNo;
    private Long walletNo;
}

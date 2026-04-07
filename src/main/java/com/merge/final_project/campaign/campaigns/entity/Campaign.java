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

    // 대표 이미지 경로(캠페인 목록 조회 시 이 경로 사용해 썸네일 바로 띄움)
    private String imagePath;

    private LocalDateTime startAt;
    private LocalDateTime endAt;
    private LocalDateTime usageStartAt;
    private LocalDateTime usageEndAt;

    private Long targetAmount;
    private Long currentAmount;

    private LocalDateTime achievedAt;
    private String category;

    // 승인 상태
    @Enumerated(EnumType.STRING)
    private ApprovalStatus approvalStatus;

    private LocalDateTime approvedAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // 캠페인 진행 상태
    @Enumerated(EnumType.STRING)
    private CampaignStatus campaignStatus;

    private String rejectReason;

    private Long foundationNo;
    private Long walletNo;
}

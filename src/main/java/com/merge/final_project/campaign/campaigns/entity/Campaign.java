package com.merge.final_project.campaign.campaigns.entity;

import com.merge.final_project.campaign.campaigns.ApprovalStatus;
import com.merge.final_project.campaign.campaigns.CampaignStatus;
import com.merge.final_project.org.foundation.Foundation;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "campaign")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Campaign {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long campaignNo;

    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    // 대표 이미지 경로(캠페인 목록 조회 시 이걸 사용해 썸네일 바로 띄움)
    private String imagePath;

    private LocalDateTime startAt;
    private LocalDateTime endAt;
    private LocalDateTime usageStartAt;
    private LocalDateTime usageEndAt;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "foundation_no", insertable = false, updatable = false)
    private Foundation foundation;

    @Column(name = "foundation_no")
    private Long foundationNo;

    @Column(name = "wallet_no")
    private Long walletNo;
}

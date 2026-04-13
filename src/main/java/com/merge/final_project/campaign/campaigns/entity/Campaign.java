package com.merge.final_project.campaign.campaigns.entity;

import com.merge.final_project.campaign.campaigns.ApprovalStatus;
import com.merge.final_project.campaign.campaigns.CampaignStatus;
import com.merge.final_project.global.BaseCreatedAtEntity;
import com.merge.final_project.org.Foundation;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "campaign")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Campaign extends BaseCreatedAtEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "campaign_no")
    private Long campaignNo;

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
    private Long targetAmount;

    @Column(name = "current_amount")
    private Long currentAmount;

    @Column(name = "achieved_at")
    private LocalDateTime achievedAt;

    private String category;

    @Enumerated(EnumType.STRING)
    @Column(name = "approval_status")
    private ApprovalStatus approvalStatus;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "campaign_status")
    private CampaignStatus campaignStatus;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "foundation_no", insertable = false, updatable = false)
    private Foundation foundation;

    @Column(name = "foundation_no")
    private Long foundationNo;

    @Column(name = "wallet_no")
    private Long walletNo;

    @Column(name = "beneficiary_no")
    private Long beneficiaryNo;

    @Column(name = "reject_reason")
    private String rejectReason;


    // [가빈] 캠페인 승인 시 상태 변경 메서드
    public void approve() {
        this.approvalStatus = ApprovalStatus.APPROVED;
        this.campaignStatus = CampaignStatus.RECRUITING;
        this.approvedAt = LocalDateTime.now();
    }

    // [가빈] 캠페인 반려 시 상태 및 사유 변경 메서드
    public void reject(String reason) {
        this.approvalStatus = ApprovalStatus.REJECTED;
        this.rejectReason = reason;
    }
}

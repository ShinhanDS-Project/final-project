package com.merge.final_project.campaign.campaigns.entity;

import com.merge.final_project.campaign.campaigns.ApprovalStatus;
import com.merge.final_project.campaign.campaigns.CampaignCategory;
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

import java.math.BigDecimal;
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
    private BigDecimal currentAmount;

    @Column(name = "achieved_at")
    private LocalDateTime achievedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "category")
    private CampaignCategory category;

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

    //[이채원] 이거 왜 빠져있는걸까요?
    @Column(name = "created_at")
    private LocalDateTime createdAt;

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
    //[채원] add함수
    public BigDecimal addCurrentAmount(BigDecimal amount) {
        this.currentAmount = this.currentAmount.add(amount);
        return this.currentAmount;
    }

    // [가빈] 캠페인 승인 후 모금시작일 되면 상태 변경하기 위해 사용하는 메서드
    public void active(){
        this.campaignStatus = CampaignStatus.ACTIVE;
    }

    // [가빈] 캠페인 모금 종료일이 되면 상태 변경하기 위해 사용하는 메서드
    public void ended(){
        this.campaignStatus = CampaignStatus.ENDED;
    }

    // [가빈] 활동 보고서 승인 이후 캠페인최종 완료 처리하기 위해 사용하는 메서드
    public void complete() {
        this.campaignStatus = CampaignStatus.COMPLETED;
    }
}

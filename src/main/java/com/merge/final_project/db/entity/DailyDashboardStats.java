package com.merge.final_project.db.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity(name = "DbDailyDashboardStats")
@Table(name = "daily_dashboard_stats")
@Getter
@Setter
@NoArgsConstructor
public class DailyDashboardStats {

    @Id
    @Column(name = "daily_no")
    private Integer dailyNo;

    @Column(name = "stats_date")
    private LocalDateTime statsDate;

    @Column(name = "daily_donation_amount")
    private Integer dailyDonationAmount;

    @Column(name = "daily_donation_count")
    private Integer dailyDonationCount;

    @Column(name = "active_campaign_count")
    private Integer activeCampaignCount;

    @Column(name = "achieved_campaign_count")
    private Integer achievedCampaignCount;

    @Column(name = "new_user_count")
    private Integer newUserCount;

    @Column(name = "new_foundation_count")
    private Integer newFoundationCount;

    @Column(name = "pending_foundation_count")
    private Integer pendingFoundationCount;

    @Column(name = "pending_inquiry_count")
    private Integer pendingInquiryCount;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}

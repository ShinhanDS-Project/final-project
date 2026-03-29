package com.merge.final_project.admin.stats;

import com.merge.final_project.global.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "daily_dashboard_stats")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DailyDashboardStats extends BaseEntity {

    @Id
    @Column(name = "daily_no")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long dailyNo;

    @Column(name = "stats_date")
    private LocalDate statsDate;

    @Column(name = "daily_donation_amount")
    private String dailyDonationAmount;

    @Column(name = "daily_donation_count")
    private Long dailyDonationCount;

    @Column(name = "active_campaign_count")
    private Long activeCampaignCount;

    @Column(name = "achived_campaign_count")
    private Long achivedCampaignCount;

    @Column(name = "new_user_count")
    private Long newUserCount;

    @Column(name = "new_foundation_count")
    private Long newFoundationCount;

    @Column(name = "pending_foundation_count")
    private Long pendingFoundationCount;

    @Column(name = "pending_inquiry_count")
    private Long pendingInquiryCount;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}

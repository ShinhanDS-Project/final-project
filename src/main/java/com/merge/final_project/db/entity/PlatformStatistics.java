package com.merge.final_project.db.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity(name = "DbPlatformStatistics")
@Table(name = "platform_statistics")
@Getter
@Setter
@NoArgsConstructor
public class PlatformStatistics {

    @Id
    @Column(name = "platform_stats_no")
    private Integer platformStatsNo;

    @Column(name = "total_donation_amount")
    private Integer totalDonationAmount;

    @Column(name = "total_donation_count")
    private Integer totalDonationCount;

    @Column(name = "total_user_count")
    private Integer totalUserCount;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}

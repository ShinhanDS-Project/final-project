package com.merge.final_project.db.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity(name = "DbCategoryStats")
@Table(name = "category_stats")
@Getter
@Setter
@NoArgsConstructor
public class CategoryStats {

    @Id
    @Column(name = "category_stats_no")
    private Integer categoryStatsNo;

    @Column(name = "stats_date")
    private LocalDateTime statsDate;

    @Column(name = "category_name")
    private String categoryName;

    @Column(name = "category_campaign_count")
    private Integer categoryCampaignCount;

    @Column(name = "donation_amount")
    private Integer donationAmount;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}

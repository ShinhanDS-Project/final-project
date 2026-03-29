package com.merge.final_project.admin.stats;

import com.merge.final_project.global.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "category_stats")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryStats extends BaseEntity {

    @Id
    @Column(name = "category_stats_no")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long categoryStatsNo;

    @Column(name = "stats_date")
    private LocalDate statsDate;

    @Column(name = "category_name")
    private String categoryName;

    @Column(name = "category_campaign_count")
    private Long categoryCampaignCount;

    @Column(name = "donation_amount")
    private String donationAmount;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}

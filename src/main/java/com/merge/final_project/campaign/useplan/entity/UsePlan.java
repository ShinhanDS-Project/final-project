package com.merge.final_project.campaign.useplan.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "use_plan")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UsePlan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "use_plan_no")
    private Integer usePlanNo;

    @Column(name = "plan_content", nullable = false)
    private String planContent;

    @Column(name = "plan_amount", nullable = false)
    private Integer planAmount;

    @Column(name = "campaign_no", nullable = false)
    private Integer campaignNo;
}

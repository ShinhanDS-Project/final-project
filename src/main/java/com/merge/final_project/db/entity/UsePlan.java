package com.merge.final_project.db.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity(name = "DbUsePlan")
@Table(name = "use_plan")
@Getter
@Setter
@NoArgsConstructor
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

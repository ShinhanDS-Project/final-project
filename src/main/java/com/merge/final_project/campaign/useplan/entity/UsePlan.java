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
    private Long usePlanNo;

    private String planContent;
    private Integer planAmount;
    private Long campaignNo;

}

package com.merge.final_project.campaign.campaigninterest;

import com.merge.final_project.global.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "interest")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CampaignInterest extends BaseEntity {

    @Id
    @Column(name = "interest_no")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long interestNo;

    @Column(name = "user_no")
    private Long userNo;

    @Column(name = "campaign_no")
    private Long campaignNo;
}

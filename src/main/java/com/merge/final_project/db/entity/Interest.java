package com.merge.final_project.db.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity(name = "DbInterest")
@Table(name = "interest")
@Getter
@Setter
@NoArgsConstructor
public class Interest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "interest_no")
    private Integer interestNo;

    @Column(name = "user_no", nullable = false)
    private Integer userNo;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "campaign_no", nullable = false)
    private Integer campaignNo;
}

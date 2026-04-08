package com.merge.final_project.db.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity(name = "DbFinalReport")
@Table(name = "final_report")
@Getter
@Setter
@NoArgsConstructor
public class FinalReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "report_no")
    private Integer reportNo;

    @Column(name = "\"content\"", nullable = false)
    private String content;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "image_path")
    private String imagePath;

    @Column(name = "usage_purpose", nullable = false)
    private String usagePurpose;

    @Column(name = "settlement_no")
    private Integer settlementNo;

    @Column(name = "approval_status")
    private String approvalStatus;

    @Column(name = "beneficiary_no", nullable = false)
    private Integer beneficiaryNo;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "due_at")
    private LocalDateTime dueAt;

    @Column(name = "reject_reason")
    private String rejectReason;

    @Column(name = "campaign_no", nullable = false)
    private Integer campaignNo;

    @Column(name = "key_no")
    private Integer keyNo;
}

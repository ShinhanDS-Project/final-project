package com.merge.final_project.report.finalreport;


import com.merge.final_project.campaign.campaigns.Campaign;
import com.merge.final_project.recipient.beneficiary.Beneficiary;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "final_report")
@Getter
@NoArgsConstructor
public class FinalReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long reportNo;

    @Column(name = "content")
    private String content;

    @Column(name = "title")
    private String title;

    @Column(name = "image_path")
    private String imagePath;

    @Column(name = "usage_purpose")
    private String usagePurpose;

    @Column(name = "settlement_no")
    private String settlementNo;

    @Enumerated(EnumType.STRING)
    @Column(name = "approval_status")
    private ReportApprovalStatus approvalStatus;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "campaign_no", nullable = false)
    private Campaign campaign;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "beneficiary_no", nullable = false)
    private Beneficiary beneficiary;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "due_at")
    private LocalDateTime dueAt;

    @Column(name = "reject_reason")
    private String rejectReason;
}
package com.merge.final_project.report.finalreport.entitiy;

import com.merge.final_project.report.finalreport.ReportApprovalStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "final_report")
@Getter
@Builder
@AllArgsConstructor
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
    private Long settlementNo;

    @Enumerated(EnumType.STRING)
    @Column(name = "approval_status")
    private ReportApprovalStatus approvalStatus;

    @Column(name = "beneficiary_no")
    private Long beneficiary_no;


    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "due_at")
    private LocalDateTime dueAt;

    @Column(name = "reject_reason")
    private String rejectReason;


    @Column(name = "campaign_no")
    private Long campaign_no;

    @Column(name = "key_no")
    private Long key_no;


    public void updateContent(String title, String content, String usagePurpose) {
        this.title = title;
        this.content = content;
        this.usagePurpose = usagePurpose;
        //수정하면 다시 대기 상태
        this.approvalStatus = ReportApprovalStatus.PENDING;
    }
}
package com.merge.final_project.report.finalreport.dto;

import com.merge.final_project.report.finalreport.ReportApprovalStatus;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class FinalReportMicroTrackingResponseDto {
    FinalReportData reportData;
    Long dayPassed;
    Boolean isExist;
    Boolean isPassed;
    String trackingStatus;

    @Getter
    @Setter
    @Builder
    public static class FinalReportData {
        private String title;
        private String content;
        private String reportFileUrl;
        private ReportApprovalStatus approvalStatus;
    }
}

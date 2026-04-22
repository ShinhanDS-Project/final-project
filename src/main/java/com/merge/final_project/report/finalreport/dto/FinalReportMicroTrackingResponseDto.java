package com.merge.final_project.report.finalreport.dto;

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
    Boolean isPassed; //지났는지 여부

    @Getter
    @Setter
    @Builder
    public static class FinalReportData {
        private String title;
        private String content;
        private String reportFileUrl; // 예시: 리포트 파일 경로 등

    }
}

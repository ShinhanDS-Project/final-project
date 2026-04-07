package com.merge.final_project.report.finalreport.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FinalReportRequestDTO {
    private Long campaign_no; // 보고서를 쓸 캠페인 번호 (999 등)
    private String title;       // 보고서 제목
    private String content;     // 보고서 내용
    private String usagePurpose; // 사용 목적 (엔티티에 있던 필드)
}
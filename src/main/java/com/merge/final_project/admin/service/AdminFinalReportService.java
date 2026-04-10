package com.merge.final_project.admin.service;

import com.merge.final_project.report.finalreport.dto.FinalReportResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AdminFinalReportService {
    //승인 대기 중인 활동 보고서 목록
    Page<FinalReportResponseDTO> getPendingReports(Pageable pageable);
    //보고서 승인하기
    void approveReport(Long reportNo);
    //보고서 반려하기
    void rejectReport(Long reportNo, String reason);
}

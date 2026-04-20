package com.merge.final_project.admin.service;

import com.merge.final_project.report.finalreport.dto.FinalReportResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AdminFinalReportService {
    Page<FinalReportResponseDTO> getPendingReports(Pageable pageable);
    FinalReportResponseDTO getReport(Long reportNo);
    void approveReport(Long reportNo);
    void rejectReport(Long reportNo, String reason);
}

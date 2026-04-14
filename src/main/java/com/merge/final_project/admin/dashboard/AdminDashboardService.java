package com.merge.final_project.admin.dashboard;

import com.merge.final_project.admin.dashboard.dto.CategoryRatioDTO;
import com.merge.final_project.admin.dashboard.dto.DashboardSummaryDTO;
import com.merge.final_project.admin.dashboard.dto.DonationTrendDTO;
import com.merge.final_project.admin.adminlog.AdminLogResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface AdminDashboardService {

    DashboardSummaryDTO getSummary();

    List<DonationTrendDTO> getDonationTrend(int days);

    List<CategoryRatioDTO> getCategoryRatio();

    Page<AdminLogResponseDTO> getRecentLogs(Pageable pageable);
}

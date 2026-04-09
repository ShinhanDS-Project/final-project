package com.merge.final_project.admin.adminlog;

import com.merge.final_project.admin.Admin;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;

public interface AdminLogService {
    void log(ActionType actionType, TargetType targetType, Long targetNo, String description, Admin admin);
    Page<AdminLogResponseDTO> getLogsWithFilter(ActionType actionType, TargetType targetType, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);
}

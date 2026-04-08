package com.merge.final_project.admin.adminlog;

import com.merge.final_project.admin.Admin;

public interface AdminLogService {
    void log(ActionType actionType, TargetType targetType, Long targetNo, String description, Admin admin);
}

package com.merge.final_project.admin.adminlog;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class AdminLogResponseDTO {

    private Long logNo;
    private String adminId;
    private String adminName;
    private ActionType actionType;
    private TargetType targetType;
    private Long targetNo;
    private String description;
    private LocalDateTime createdAt;

    public static AdminLogResponseDTO from(AdminLog log) {
        return AdminLogResponseDTO.builder()
                .logNo(log.getLogNo())
                .adminId(log.getAdmin().getAdminId())
                .adminName(log.getAdmin().getName())
                .actionType(log.getActionType())
                .targetType(log.getTargetType())
                .targetNo(log.getTargetNo())
                .description(log.getDescription())
                .createdAt(log.getCreatedAt())
                .build();
    }
}

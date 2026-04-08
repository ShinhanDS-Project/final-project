package com.merge.final_project.admin.sse;

import com.merge.final_project.admin.adminlog.TargetType;
import lombok.Getter;

@Getter
public class ApprovalRequestEvent {

    private final TargetType targetType;
    private final Long targetId;
    private final String message;

    public ApprovalRequestEvent(TargetType targetType, Long targetId, String message) {
        this.targetType = targetType;
        this.targetId = targetId;
        this.message = message;
    }
}

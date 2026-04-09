package com.merge.final_project.admin.sse;

import com.merge.final_project.admin.adminlog.TargetType;
import lombok.Getter;

@Getter
public class ApprovalRequestEvent {

    private final TargetType targetType;
    private final Long targetId;
    private final String message;

    //호출하는 곳에서 이벤트 발생하면 해당 이벤트 클래스 생성. 이후 해당 객체 정보가 sse통신에서 브로드캐스팅 됨.
    public ApprovalRequestEvent(TargetType targetType, Long targetId, String message) {
        this.targetType = targetType;
        this.targetId = targetId;
        this.message = message;
    }
}

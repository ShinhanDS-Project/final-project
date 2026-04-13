package com.merge.final_project.notification.email.event;

import lombok.AllArgsConstructor;
import lombok.Getter;

// [가빈] 관리자가 직접 기부단체를 비활성화할 때 발행하는 이벤트.
// 배치 비활성화(FoundationInactiveEvent)와 분리 — 배치용은 캠페인 제목이 필요하지만 이건 불필요함.
@AllArgsConstructor
@Getter
public class FoundationDeactivatedByAdminEvent {
    private final String email;
    private final String foundationName;
}

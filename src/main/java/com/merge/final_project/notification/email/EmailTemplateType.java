package com.merge.final_project.notification.email;

public enum EmailTemplateType {
    DONATION_COMPLETE, CAMPAIGN_APPROVED, CAMPAIGN_REJECTED,
    CAMPAIGN_ACHIEVED, PASSWORD_RESET, ACCOUNT_APPROVED, ACCOUNT_REJECTED,
    FOUNDATION_TEMP_PASSWORD,
    FOUNDATION_INACTIVE_BATCH,       // 배치 자동 비활성화 (활동 보고서 14일 미제출)
    FOUNDATION_DEACTIVATED_BY_ADMIN  // 관리자 직접 비활성화
}

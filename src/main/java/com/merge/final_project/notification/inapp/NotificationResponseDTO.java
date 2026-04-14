package com.merge.final_project.notification.inapp;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Schema(description = "인앱 알림 응답 DTO")
@Getter
@Builder
public class NotificationResponseDTO {

    @Schema(description = "알림 번호", example = "1")
    private Long notificationNo;

    @Schema(description = "알림 유형 (CAMPAIGN_APPROVED, CAMPAIGN_REJECTED, REPORT_APPROVED, REPORT_REJECTED 등)", example = "CAMPAIGN_APPROVED")
    private NotificationType notificationType;

    @Schema(description = "알림 내용", example = "캠페인 '어린이 급식 지원'이 승인되었습니다.")
    private String content;

    @Schema(description = "읽음 여부 (true: 읽음, false: 미읽음)", example = "false")
    private boolean isRead;

    @Schema(description = "알림 생성 일시", example = "2024-01-15T10:30:00")
    private LocalDateTime createdAt;

    @Schema(description = "읽음 처리 일시 (미읽음이면 null)", example = "2024-01-15T11:00:00")
    private LocalDateTime readAt;

    public static NotificationResponseDTO from(Notification notification) {
        return NotificationResponseDTO.builder()
                .notificationNo(notification.getNotificationNo())
                .notificationType(notification.getNotificationType())
                .content(notification.getContent())
                .isRead(notification.isRead())
                .createdAt(notification.getCreatedAt())
                .readAt(notification.getReadAt())
                .build();
    }
}

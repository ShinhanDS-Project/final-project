package com.merge.final_project.notification.inapp;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class NotificationResponseDTO {

    private Long notificationNo;
    private NotificationType notificationType;
    private String content;
    private boolean isRead;
    private LocalDateTime sentAt;
    private LocalDateTime readAt;

    public static NotificationResponseDTO from(Notification notification) {
        return NotificationResponseDTO.builder()
                .notificationNo(notification.getNotificationNo())
                .notificationType(notification.getNotificationType())
                .content(notification.getContent())
                .isRead(notification.isRead())
                .sentAt(notification.getSentAt())
                .readAt(notification.getReadAt())
                .build();
    }
}

package com.merge.final_project.notification.inapp;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class AdminNotificationResponseDTO {

    private Long notificationNo;
    private Long receiverNo;
    private RecipientType recipientType;
    private NotificationType notificationType;
    private String content;
    private boolean isRead;
    private LocalDateTime createdAt;
    private LocalDateTime readAt;

    public static AdminNotificationResponseDTO from(Notification n) {
        return AdminNotificationResponseDTO.builder()
                .notificationNo(n.getNotificationNo())
                .receiverNo(n.getReceiverNo())
                .recipientType(n.getRecipientType())
                .notificationType(n.getNotificationType())
                .content(n.getContent())
                .isRead(n.isRead())
                .createdAt(n.getCreatedAt())
                .readAt(n.getReadAt())
                .build();
    }
}

package com.merge.final_project.notification.inapp;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;

public interface NotificationService {

    // 알림 생성 - 각 서비스에서 승인/반려/상태변경 시 호출
    void send(RecipientType recipientType, Long receiverNo, NotificationType notificationType, String content);

    // 알림 목록 조회
    Page<NotificationResponseDTO> getNotifications(RecipientType recipientType, Long receiverNo, Pageable pageable);

    // 읽지 않은 알림 수 (배지용)
    long getUnreadCount(RecipientType recipientType, Long receiverNo);

    // 단건 읽음 처리 (소유권 검증 포함)
    void markAsRead(Long notificationNo, RecipientType recipientType, Long receiverNo);

    // 전체 읽음 처리
    void markAllAsRead(RecipientType recipientType, Long receiverNo);

    Page<AdminNotificationResponseDTO> getAll(
            RecipientType recipientType,
            NotificationType notificationType,
            Boolean isRead,
            String keyword,
            LocalDate from,
            LocalDate to,
            Pageable pageable
    );
}

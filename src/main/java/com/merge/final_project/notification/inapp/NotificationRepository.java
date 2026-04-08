package com.merge.final_project.notification.inapp;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    // 수신자별 알림 목록 (최신순은 Pageable에서 정렬 처리)
    Page<Notification> findByReceiverNoAndRecipientType(Long receiverNo, RecipientType recipientType, Pageable pageable);

    // 읽지 않은 알림 수
    long countByReceiverNoAndRecipientTypeAndIsRead(Long receiverNo, RecipientType recipientType, boolean isRead);
}

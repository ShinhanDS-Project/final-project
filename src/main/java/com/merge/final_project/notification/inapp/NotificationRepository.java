package com.merge.final_project.notification.inapp;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    // 수신자별 알림 목록 (recipient_type 대소문자 무관하게 조회)
    @Query(value = "SELECT * FROM notification WHERE receiver_no = :receiverNo AND LOWER(recipient_type) = lower(:#{#recipientType.name()})",
           countQuery = "SELECT count(*) FROM notification WHERE receiver_no = :receiverNo AND LOWER(recipient_type) = lower(:#{#recipientType.name()})",
           nativeQuery = true)
    Page<Notification> findByReceiverNoAndRecipientType(@Param("receiverNo") Long receiverNo, @Param("recipientType") RecipientType recipientType, Pageable pageable);

    // 읽지 않은 알림 수
    @Query(value = "SELECT count(*) FROM notification WHERE receiver_no = :receiverNo AND LOWER(recipient_type) = lower(:#{#recipientType.name()}) AND is_read = :isRead",
           nativeQuery = true)
    long countByReceiverNoAndRecipientTypeAndIsRead(@Param("receiverNo") Long receiverNo, @Param("recipientType") RecipientType recipientType, @Param("isRead") boolean isRead);

    // 배치 중복 발송 방지: 특정 수신자에게 특정 타입의 알림이 이미 발송됐는지 확인
    @Query(value = "SELECT count(*) > 0 FROM notification WHERE receiver_no = :receiverNo AND notification_type = :#{#notificationType.name()}",
           nativeQuery = true)
    boolean existsByReceiverNoAndNotificationType(@Param("receiverNo") Long receiverNo, @Param("notificationType") NotificationType notificationType);
}

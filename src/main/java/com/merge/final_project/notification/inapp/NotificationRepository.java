package com.merge.final_project.notification.inapp;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    // 관리자용 - 알림 전체 보기

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

    // 관리자용 전체 알림 조회 - CAST(param AS text) IS NULL 패턴으로 PostgreSQL null 타입 추론 문제 우회
    @Query(value = """
            SELECT * FROM notification
            WHERE (CAST(:recipientType AS text) IS NULL OR LOWER(recipient_type) = LOWER(:recipientType))
              AND (CAST(:notificationType AS text) IS NULL OR notification_type = :notificationType)
              AND (:isRead IS NULL OR is_read = :isRead)
              AND (CAST(:keyword AS text) IS NULL OR content ILIKE '%' || :keyword || '%')
              AND (CAST(:from AS timestamp) IS NULL OR created_at >= :from)
              AND (CAST(:to AS timestamp) IS NULL OR created_at <= :to)
            """,
           countQuery = """
            SELECT count(*) FROM notification
            WHERE (CAST(:recipientType AS text) IS NULL OR LOWER(recipient_type) = LOWER(:recipientType))
              AND (CAST(:notificationType AS text) IS NULL OR notification_type = :notificationType)
              AND (:isRead IS NULL OR is_read = :isRead)
              AND (CAST(:keyword AS text) IS NULL OR content ILIKE '%' || :keyword || '%')
              AND (CAST(:from AS timestamp) IS NULL OR created_at >= :from)
              AND (CAST(:to AS timestamp) IS NULL OR created_at <= :to)
            """,
           nativeQuery = true)
    Page<Notification> findAllForAdmin(
            @Param("recipientType") String recipientType,
            @Param("notificationType") String notificationType,
            @Param("isRead") Boolean isRead,
            @Param("keyword") String keyword,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to,
            Pageable pageable
    );
}

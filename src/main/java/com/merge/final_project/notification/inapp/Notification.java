package com.merge.final_project.notification.inapp;

import com.merge.final_project.global.BaseCreatedAtEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "notification")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Notification extends BaseCreatedAtEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notification_no")
    private Long notificationNo;

    // FK 없이 receiver_no(수행대상 pk) + recipient_type(수행대상 테이블명)으로 수신자 식별 (user/foundation/beneficiary 통합)
    @Column(name = "receiver_no", nullable = false)
    private Long receiverNo;

    @Enumerated(EnumType.STRING)
    @Column(name = "recipient_type", nullable = false)
    private RecipientType recipientType;

    @Enumerated(EnumType.STRING)
    @Column(name = "notification_type", nullable = false)
    private NotificationType notificationType;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(name = "read_at")
    private LocalDateTime readAt;

    //안 읽음이 기본
    @Column(name = "is_read", columnDefinition = "boolean default false")
    private boolean isRead;

    //읽음/안읽음 상태 변경 용 메서드
    public void markAsRead() {
        this.isRead = true;
        this.readAt = LocalDateTime.now();
    }
}

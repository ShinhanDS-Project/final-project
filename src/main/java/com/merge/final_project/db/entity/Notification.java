package com.merge.final_project.db.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity(name = "DbNotification")
@Table(name = "notification")
@Getter
@Setter
@NoArgsConstructor
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notification_no")
    private Integer notificationNo;

    @Column(name = "receiver_no", nullable = false)
    private Integer receiverNo;

    @Column(name = "recipient_type", nullable = false)
    private String recipientType;

    @Column(name = "notification_type", nullable = false)
    private String notificationType;

    @Column(name = "\"content\"")
    private String content;

    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    @Column(name = "read_at")
    private LocalDateTime readAt;

    @Column(name = "is_read")
    private String isRead;
}

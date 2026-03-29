package com.merge.final_project.notification;

import com.merge.final_project.global.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "email_queue")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmailQueue extends BaseEntity {

    @Id
    @Column(name = "email_queue_no")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long emailQueueNo;

    @Column(name = "recipient_email")
    private String recipientEmail;

    @Enumerated(EnumType.STRING)
    @Column(name = "template_type")
    private EmailTemplateType templateType;

    private String title;
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(name = "email_status")
    private EmailStatus emailStatus;

    @Column(name = "retrey_count")
    private Integer retryCount;

    @Column(name = "scheduled_at")
    private LocalDateTime scheduledAt;

    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    @Column(name = "error_message")
    private String errorMessage;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}

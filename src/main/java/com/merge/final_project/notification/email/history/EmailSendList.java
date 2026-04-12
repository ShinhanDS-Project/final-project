package com.merge.final_project.notification.email.history;

import com.merge.final_project.global.BaseEntity;
import com.merge.final_project.notification.email.EmailStatus;
import com.merge.final_project.notification.email.EmailTemplateType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "email_send_list")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmailSendList extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "email_queue_no")
    private Long emailQueueNo;

    @Column(name = "recipient_email", nullable = false)
    private String recipientEmail;

    @Enumerated(EnumType.STRING)
    @Column(name = "template_type")
    private EmailTemplateType templateType;

    @Column(name = "title")
    private String title;

    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(name = "email_status")
    private EmailStatus emailStatus;

    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    public void markSent() {
        this.emailStatus = EmailStatus.SENT;
        this.sentAt = LocalDateTime.now();
    }

    public void markFailed() {
        this.emailStatus = EmailStatus.FAILED;
    }
}

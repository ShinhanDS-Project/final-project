package com.merge.final_project.db.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity(name = "DbEmailSendList")
@Table(name = "email_send_list")
@Getter
@Setter
@NoArgsConstructor
public class EmailSendList {

    @Id
    @Column(name = "email_queue_no")
    private Integer emailQueueNo;

    @Column(name = "recipient_email", nullable = false)
    private String recipientEmail;

    @Column(name = "template_type")
    private String templateType;

    @Column(name = "title")
    private String title;

    @Column(name = "\"content\"")
    private String content;

    @Column(name = "email_status")
    private String emailStatus;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "sent_at")
    private LocalDateTime sentAt;
}

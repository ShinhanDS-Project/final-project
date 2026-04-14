package com.merge.final_project.notification.email.history;

import com.merge.final_project.notification.email.EmailStatus;
import com.merge.final_project.notification.email.EmailTemplateType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class EmailSendListResponseDTO {
    private Long emailQueueNo;
    private String recipientEmail;
    private EmailTemplateType templateType;
    private String title;
    private EmailStatus emailStatus;
    private LocalDateTime sentAt;
    private LocalDateTime createdAt;

    public static EmailSendListResponseDTO from(EmailSendList emailSendList) {
        return EmailSendListResponseDTO.builder()
                .emailQueueNo(emailSendList.getEmailQueueNo())
                .recipientEmail(emailSendList.getRecipientEmail())
                .templateType(emailSendList.getTemplateType())
                .title(emailSendList.getTitle())
                .emailStatus(emailSendList.getEmailStatus())
                .sentAt(emailSendList.getSentAt())
                .createdAt(emailSendList.getCreatedAt())
                .build();
    }
}

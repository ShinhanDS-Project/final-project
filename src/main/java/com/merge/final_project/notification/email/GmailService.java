package com.merge.final_project.notification.email;

import org.thymeleaf.context.Context;

public interface GmailService {
    void sendSignupMail(String to, String foundationName, String tempPassword);
    void sendRejectMail(String to, String foundationName, String rejectReason);
    void sendInactiveMail(String to, String subject, String content);
}

package com.merge.final_project.notification.email;

public interface GmailService {
    void sendMail(String to, String subject, String content);
}

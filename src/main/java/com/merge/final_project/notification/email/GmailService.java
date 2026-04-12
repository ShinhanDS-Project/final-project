package com.merge.final_project.notification.email;

import java.util.concurrent.CompletableFuture;

public interface GmailService {
    CompletableFuture<Void> sendSignupMail(String to, String foundationName, String tempPassword);
    CompletableFuture<Void> sendRejectMail(String to, String foundationName, String rejectReason);
    CompletableFuture<Void> sendInactiveMail(String to, String foundationName, String campaignTitle);
}

package com.merge.final_project.notification.email;

import java.util.concurrent.CompletableFuture;

public interface GmailService {
    CompletableFuture<Void> sendSignupMail(String to, String foundationName, String tempPassword);
    CompletableFuture<Void> sendRejectMail(String to, String foundationName, String rejectReason);
    CompletableFuture<Void> sendInactiveMail(String to, String foundationName, String campaignTitle);
    // [가빈] 관리자 직접 비활성화 시 발송 (배치 비활성화와 다른 템플릿 사용)
    CompletableFuture<Void> sendDeactivateByAdminMail(String to, String foundationName);
}

package com.merge.final_project.notification.email.event;

import com.merge.final_project.notification.email.GmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.concurrent.CompletableFuture;

@Component
@RequiredArgsConstructor
public class FoundationEventListener {

    private final GmailService gmailService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public CompletableFuture<Void> handleApproved(FoundationApprovedEvent event) {
        return gmailService.sendSignupMail(event.getEmail(), event.getFoundationName(), event.getTempPassword());
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public CompletableFuture<Void> handleRejected(FoundationRejectedEvent event) {
        return gmailService.sendRejectMail(event.getEmail(), event.getFoundationName(), event.getRejectedReason());
    }
}

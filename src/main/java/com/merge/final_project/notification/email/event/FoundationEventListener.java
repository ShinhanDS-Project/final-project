package com.merge.final_project.notification.email.event;

import com.merge.final_project.notification.email.EmailStatus;
import com.merge.final_project.notification.email.EmailTemplateType;
import com.merge.final_project.notification.email.GmailService;
import com.merge.final_project.notification.email.history.EmailSendList;
import com.merge.final_project.notification.email.history.EmailSendListRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
@RequiredArgsConstructor
public class FoundationEventListener {

    private final GmailService gmailService;
    private final EmailSendListRepository emailSendListRepository;

    private static final String DELIMITER = "||";

    @Async
    @Transactional
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public CompletableFuture<Void> handleApproved(FoundationApprovedEvent event) {
        // content = "foundationName||tempPassword(평문)" — 재시도 시 필요
        EmailSendList record = emailSendListRepository.save(EmailSendList.builder()
                .recipientEmail(event.getEmail())
                .templateType(EmailTemplateType.ACCOUNT_APPROVED)
                .title("[giveNtoken] 가입 신청 승인 및 임시 비밀번호 안내")
                .content(event.getFoundationName() + DELIMITER + event.getTempPassword())
                .emailStatus(EmailStatus.PENDING)
                .build());
        trySend(() -> gmailService.sendSignupMail(event.getEmail(), event.getFoundationName(), event.getTempPassword()), record);
        return CompletableFuture.completedFuture(null);
    }

    @Async
    @Transactional
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public CompletableFuture<Void> handleRejected(FoundationRejectedEvent event) {
        // content = "foundationName||rejectReason"
        EmailSendList record = emailSendListRepository.save(EmailSendList.builder()
                .recipientEmail(event.getEmail())
                .templateType(EmailTemplateType.ACCOUNT_REJECTED)
                .title("[giveNtoken] 가입 신청 반려 안내")
                .content(event.getFoundationName() + DELIMITER + event.getRejectedReason())
                .emailStatus(EmailStatus.PENDING)
                .build());
        trySend(() -> gmailService.sendRejectMail(event.getEmail(), event.getFoundationName(), event.getRejectedReason()), record);
        return CompletableFuture.completedFuture(null);
    }

    @Async
    @Transactional
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public CompletableFuture<Void> handleInactive(FoundationInactiveEvent event) {
        // content = "foundationName||campaignTitle"
        EmailSendList record = emailSendListRepository.save(EmailSendList.builder()
                .recipientEmail(event.getEmail())
                .templateType(EmailTemplateType.FOUNDATION_INACTIVE_BATCH)
                .title("[giveNtoken] 계정 비활성화 안내")
                .content(event.getFoundationName() + DELIMITER + event.getCampaignTitle())
                .emailStatus(EmailStatus.PENDING)
                .build());
        trySend(() -> gmailService.sendInactiveMail(event.getEmail(), event.getFoundationName(), event.getCampaignTitle()), record);
        return CompletableFuture.completedFuture(null);
    }

    // [가빈] 관리자 직접 비활성화 메일 핸들러
    @Async
    @Transactional
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public CompletableFuture<Void> handleDeactivatedByAdmin(FoundationDeactivatedByAdminEvent event) {
        // content = foundationName
        EmailSendList record = emailSendListRepository.save(EmailSendList.builder()
                .recipientEmail(event.getEmail())
                .templateType(EmailTemplateType.FOUNDATION_DEACTIVATED_BY_ADMIN)
                .title("[giveNtoken] 계정 비활성화 안내")
                .content(event.getFoundationName())
                .emailStatus(EmailStatus.PENDING)
                .build());
        trySend(() -> gmailService.sendDeactivateByAdminMail(event.getEmail(), event.getFoundationName()), record);
        return CompletableFuture.completedFuture(null);
    }

    private void trySend(Runnable sendTask, EmailSendList record) {
        try {
            sendTask.run();
            record.markSent();
        } catch (Exception e) {
            record.markFailed();
            log.error("메일 발송 실패 - recipientEmail: {}, templateType: {}", record.getRecipientEmail(), record.getTemplateType());
        }
    }
}

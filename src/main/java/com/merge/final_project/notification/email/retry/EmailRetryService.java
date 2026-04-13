package com.merge.final_project.notification.email.retry;

import com.merge.final_project.notification.email.EmailStatus;
import com.merge.final_project.notification.email.GmailService;
import com.merge.final_project.notification.email.history.EmailSendList;
import com.merge.final_project.notification.email.history.EmailSendListRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailRetryService {

    private static final String DELIMITER = "||";

    private final EmailSendListRepository emailSendListRepository;
    private final GmailService gmailService;

    @Transactional
    public void retryFailed() {
        List<EmailSendList> failedList = emailSendListRepository.findByEmailStatus(EmailStatus.FAILED);

        if (failedList.isEmpty()) {
            log.info("재시도 대상 메일 없음");
            return;
        }

        log.info("메일 재시도 시작 - 대상 건수: {}", failedList.size());

        for (EmailSendList record : failedList) {
            try {
                resend(record);
                record.markSent();
                log.info("메일 재시도 성공 emailQueueNo={}", record.getEmailQueueNo());
            } catch (Exception e) {
                record.markFailed();
                log.error("메일 재시도 실패 emailQueueNo={}, templateType={}", record.getEmailQueueNo(), record.getTemplateType());
            }
        }
    }

    private void resend(EmailSendList record) {
        String email = record.getRecipientEmail();
        String[] parts = record.getContent() != null ? record.getContent().split("\\|\\|", 2) : new String[]{};

        switch (record.getTemplateType()) {
            case ACCOUNT_APPROVED -> {
                // content = "foundationName||tempPassword"
                String foundationName = parts[0];
                String tempPassword = parts[1];
                gmailService.sendSignupMail(email, foundationName, tempPassword);
            }
            case ACCOUNT_REJECTED -> {
                // content = "foundationName||rejectReason"
                String foundationName = parts[0];
                String rejectReason = parts[1];
                gmailService.sendRejectMail(email, foundationName, rejectReason);
            }
            case FOUNDATION_INACTIVE_BATCH -> {
                // content = "foundationName||campaignTitle"
                String foundationName = parts[0];
                String campaignTitle = parts[1];
                gmailService.sendInactiveMail(email, foundationName, campaignTitle);
            }
            case FOUNDATION_DEACTIVATED_BY_ADMIN -> {
                // content = foundationName
                String foundationName = parts[0];
                gmailService.sendDeactivateByAdminMail(email, foundationName);
            }
            default -> throw new IllegalArgumentException("재시도 미지원 templateType: " + record.getTemplateType());
        }
    }
}

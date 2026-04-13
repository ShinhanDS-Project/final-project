package com.merge.final_project.notification;

import com.merge.final_project.notification.email.EmailStatus;
import com.merge.final_project.notification.email.EmailTemplateType;
import com.merge.final_project.notification.email.GmailService;
import com.merge.final_project.notification.email.event.*;
import com.merge.final_project.notification.email.history.EmailSendList;
import com.merge.final_project.notification.email.history.EmailSendListRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FoundationEventListenerTest {

    @InjectMocks
    private FoundationEventListener foundationEventListener;

    @Mock
    private GmailService gmailService;

    @Mock
    private EmailSendListRepository emailSendListRepository;

    @Test
    @DisplayName("승인 이벤트 발생 시 메일이 발송되고 ACCOUNT_APPROVED 내역이 저장된다")
    void 승인_메일_발송_내역저장() {
        FoundationApprovedEvent event = new FoundationApprovedEvent(1L, "test@test.com", "테스트단체", "tempPw");

        foundationEventListener.handleApproved(event);

        verify(gmailService).sendSignupMail("test@test.com", "테스트단체", "tempPw");

        ArgumentCaptor<EmailSendList> captor = ArgumentCaptor.forClass(EmailSendList.class);
        verify(emailSendListRepository).save(captor.capture());
        assertThat(captor.getValue().getTemplateType()).isEqualTo(EmailTemplateType.ACCOUNT_APPROVED);
        assertThat(captor.getValue().getEmailStatus()).isEqualTo(EmailStatus.SENT);
        assertThat(captor.getValue().getRecipientEmail()).isEqualTo("test@test.com");
    }

    @Test
    @DisplayName("반려 이벤트 발생 시 메일이 발송되고 ACCOUNT_REJECTED 내역이 저장된다")
    void 반려_메일_발송_내역저장() {
        FoundationRejectedEvent event = new FoundationRejectedEvent(1L, "test@test.com", "테스트단체", "기부금 횡령");

        foundationEventListener.handleRejected(event);

        verify(gmailService).sendRejectMail("test@test.com", "테스트단체", "기부금 횡령");

        ArgumentCaptor<EmailSendList> captor = ArgumentCaptor.forClass(EmailSendList.class);
        verify(emailSendListRepository).save(captor.capture());
        assertThat(captor.getValue().getTemplateType()).isEqualTo(EmailTemplateType.ACCOUNT_REJECTED);
        assertThat(captor.getValue().getEmailStatus()).isEqualTo(EmailStatus.SENT);
    }

    @Test
    @DisplayName("배치 비활성화 이벤트 발생 시 메일이 발송되고 FOUNDATION_INACTIVE_BATCH 내역이 저장된다")
    void 배치_비활성화_메일_발송_내역저장() {
        FoundationInactiveEvent event = new FoundationInactiveEvent("test@test.com", "테스트단체", "캠페인A");

        foundationEventListener.handleInactive(event);

        verify(gmailService).sendInactiveMail("test@test.com", "테스트단체", "캠페인A");

        ArgumentCaptor<EmailSendList> captor = ArgumentCaptor.forClass(EmailSendList.class);
        verify(emailSendListRepository).save(captor.capture());
        assertThat(captor.getValue().getTemplateType()).isEqualTo(EmailTemplateType.FOUNDATION_INACTIVE_BATCH);
        assertThat(captor.getValue().getEmailStatus()).isEqualTo(EmailStatus.SENT);
    }

    @Test
    @DisplayName("관리자 직접 비활성화 이벤트 발생 시 메일이 발송되고 FOUNDATION_DEACTIVATED_BY_ADMIN 내역이 저장된다")
    void 관리자_비활성화_메일_발송_내역저장() {
        FoundationDeactivatedByAdminEvent event = new FoundationDeactivatedByAdminEvent("test@test.com", "테스트단체");

        foundationEventListener.handleDeactivatedByAdmin(event);

        verify(gmailService).sendDeactivateByAdminMail("test@test.com", "테스트단체");

        ArgumentCaptor<EmailSendList> captor = ArgumentCaptor.forClass(EmailSendList.class);
        verify(emailSendListRepository).save(captor.capture());
        assertThat(captor.getValue().getTemplateType()).isEqualTo(EmailTemplateType.FOUNDATION_DEACTIVATED_BY_ADMIN);
        assertThat(captor.getValue().getEmailStatus()).isEqualTo(EmailStatus.SENT);
    }

    @Test
    @DisplayName("메일 발송 실패 시 내역이 저장되지 않는다")
    void 메일_발송_실패_내역미저장() {
        FoundationApprovedEvent event = new FoundationApprovedEvent(1L, "test@test.com", "테스트단체", "tempPw");
        doThrow(new RuntimeException("메일 서버 오류")).when(gmailService)
                .sendSignupMail(anyString(), anyString(), anyString());

        org.junit.jupiter.api.Assertions.assertThrows(RuntimeException.class,
                () -> foundationEventListener.handleApproved(event));

        verify(emailSendListRepository, never()).save(any());
    }
}

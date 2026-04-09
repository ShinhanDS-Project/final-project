package com.merge.final_project.notification;

import com.merge.final_project.global.exceptions.BusinessException;
import com.merge.final_project.global.exceptions.ErrorCode;
import com.merge.final_project.notification.inapp.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @InjectMocks
    private NotificationServiceImpl notificationService;

    @Mock
    private NotificationRepository notificationRepository;

    @Test
    @DisplayName("알림이 올바른 정보로 저장된다")
    void 알림_저장() {
        notificationService.send(RecipientType.USERS, 1L, NotificationType.CAMPAIGN_APPROVED, "캠페인이 승인되었습니다.");

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).save(captor.capture());

        Notification saved = captor.getValue();
        assertThat(saved.getRecipientType()).isEqualTo(RecipientType.USERS);
        assertThat(saved.getReceiverNo()).isEqualTo(1L);
        assertThat(saved.getNotificationType()).isEqualTo(NotificationType.CAMPAIGN_APPROVED);
        assertThat(saved.getContent()).isEqualTo("캠페인이 승인되었습니다.");
        assertThat(saved.isRead()).isFalse();
    }

    @Test
    @DisplayName("읽지 않은 알림 수를 반환한다")
    void 읽지않은_알림_수() {
        when(notificationRepository.countByReceiverNoAndRecipientTypeAndIsRead(1L, RecipientType.USERS, false))
                .thenReturn(3L);

        long count = notificationService.getUnreadCount(RecipientType.USERS, 1L);

        assertThat(count).isEqualTo(3L);
    }

    @Test
    @DisplayName("단건 읽음 처리 시 읽음 상태로 변경된다")
    void 단건_읽음_처리() {
        Notification notification = Notification.builder()
                .receiverNo(1L)
                .recipientType(RecipientType.USERS)
                .notificationType(NotificationType.CAMPAIGN_APPROVED)
                .content("내용")
                .isRead(false)
                .build();

        when(notificationRepository.findById(1L)).thenReturn(Optional.of(notification));

        notificationService.markAsRead(1L);

        assertThat(notification.isRead()).isTrue();
        assertThat(notification.getReadAt()).isNotNull();
    }

    @Test
    @DisplayName("존재하지 않는 알림 읽음 처리 시 예외가 발생한다")
    void 존재하지않는_알림_읽음처리_예외() {
        when(notificationRepository.findById(999L)).thenReturn(Optional.empty());

        BusinessException exception = assertThrows(BusinessException.class,
                () -> notificationService.markAsRead(999L));

        assertThat(exception.getMessage()).isEqualTo(ErrorCode.NOTIFICATION_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("전체 읽음 처리 시 읽지 않은 알림만 읽음 상태로 변경된다")
    void 전체_읽음_처리() {
        Notification unread1 = Notification.builder()
                .receiverNo(1L).recipientType(RecipientType.USERS)
                .notificationType(NotificationType.CAMPAIGN_APPROVED)
                .content("내용1").isRead(false).build();

        Notification unread2 = Notification.builder()
                .receiverNo(1L).recipientType(RecipientType.USERS)
                .notificationType(NotificationType.FOUNDATION_APPROVED)
                .content("내용2").isRead(false).build();

        Notification alreadyRead = Notification.builder()
                .receiverNo(1L).recipientType(RecipientType.USERS)
                .notificationType(NotificationType.CAMPAIGN_REJECTED)
                .content("내용3").isRead(true).build();

        when(notificationRepository.findByReceiverNoAndRecipientType(1L, RecipientType.USERS, Pageable.unpaged()))
                .thenReturn(new PageImpl<>(List.of(unread1, unread2, alreadyRead)));

        notificationService.markAllAsRead(RecipientType.USERS, 1L);

        assertThat(unread1.isRead()).isTrue();
        assertThat(unread2.isRead()).isTrue();
        assertThat(alreadyRead.getReadAt()).isNull(); // 이미 읽은 건 readAt 변경 없음
    }
}

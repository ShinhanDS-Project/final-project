package com.merge.final_project.notification;

import com.merge.final_project.notification.inapp.Notification;
import com.merge.final_project.notification.inapp.NotificationRepository;
import com.merge.final_project.notification.inapp.NotificationType;
import com.merge.final_project.notification.inapp.RecipientType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class NotificationRepositoryTest {

    @Autowired
    private NotificationRepository notificationRepository;

    private Notification notification(Long receiverNo, RecipientType recipientType, boolean isRead) {
        return Notification.builder()
                .receiverNo(receiverNo)
                .recipientType(recipientType)
                .notificationType(NotificationType.FOUNDATION_APPROVED)
                .content("테스트 알림")
                .isRead(isRead)
                .build();
    }

    @Test
    @DisplayName("수신자 번호와 타입으로 알림 목록을 조회할 수 있다")
    void 수신자_알림_목록_조회() {
        notificationRepository.save(notification(1L, RecipientType.USERS, false));
        notificationRepository.save(notification(1L, RecipientType.USERS, false));
        notificationRepository.save(notification(2L, RecipientType.USERS, false)); // 다른 수신자

        Page<Notification> result = notificationRepository
                .findByReceiverNoAndRecipientType(1L, RecipientType.USERS, PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent()).allMatch(n -> n.getReceiverNo().equals(1L));
    }

    @Test
    @DisplayName("다른 recipientType의 알림은 조회되지 않는다")
    void 다른_타입_알림_미조회() {
        notificationRepository.save(notification(1L, RecipientType.USERS, false));
        notificationRepository.save(notification(1L, RecipientType.FOUNDATION, false));

        Page<Notification> result = notificationRepository
                .findByReceiverNoAndRecipientType(1L, RecipientType.USERS, PageRequest.of(0, 10));

        assertThat(result.getContent()).allMatch(n -> n.getRecipientType() == RecipientType.USERS);
    }

    @Test
    @DisplayName("읽지 않은 알림 수를 정확히 반환한다")
    void 읽지않은_알림_수_조회() {
        notificationRepository.save(notification(1L, RecipientType.USERS, false));
        notificationRepository.save(notification(1L, RecipientType.USERS, false));
        notificationRepository.save(notification(1L, RecipientType.USERS, true)); // 읽은 알림

        long count = notificationRepository
                .countByReceiverNoAndRecipientTypeAndIsRead(1L, RecipientType.USERS, false);

        assertThat(count).isEqualTo(2);
    }

    @Test
    @DisplayName("읽은 알림은 읽지 않은 알림 수에 포함되지 않는다")
    void 읽은_알림_카운트_제외() {
        notificationRepository.save(notification(1L, RecipientType.USERS, true));
        notificationRepository.save(notification(1L, RecipientType.USERS, true));

        long count = notificationRepository
                .countByReceiverNoAndRecipientTypeAndIsRead(1L, RecipientType.USERS, false);

        assertThat(count).isZero();
    }
}

package com.merge.final_project.notification.inapp;

import com.merge.final_project.global.exceptions.BusinessException;
import com.merge.final_project.global.exceptions.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;

    @Override
    @Transactional
    public void send(RecipientType recipientType, Long receiverNo, NotificationType notificationType, String content) {
        Notification notification = Notification.builder()
                .recipientType(recipientType)
                .receiverNo(receiverNo)
                .notificationType(notificationType)
                .content(content)
                .sentAt(LocalDateTime.now())
                .isRead(false)
                .build();
        notificationRepository.save(notification);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<NotificationResponseDTO> getNotifications(RecipientType recipientType, Long receiverNo, Pageable pageable) {
        return notificationRepository
                .findByReceiverNoAndRecipientType(receiverNo, recipientType, pageable)
                .map(NotificationResponseDTO::from);
    }

    @Override
    @Transactional(readOnly = true)
    public long getUnreadCount(RecipientType recipientType, Long receiverNo) {
        return notificationRepository
                .countByReceiverNoAndRecipientTypeAndIsRead(receiverNo, recipientType, false);
    }

    @Override
    @Transactional
    public void markAsRead(Long notificationNo) {
        Notification notification = notificationRepository.findById(notificationNo)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOTIFICATION_NOT_FOUND));
        notification.markAsRead();
    }

    @Override
    @Transactional
    public void markAllAsRead(RecipientType recipientType, Long receiverNo) {
        notificationRepository
                .findByReceiverNoAndRecipientType(receiverNo, recipientType, Pageable.unpaged())
                .filter(n -> !n.isRead())
                .forEach(Notification::markAsRead);
    }
}

package com.merge.final_project.notification.inapp;

import com.merge.final_project.global.exceptions.BusinessException;
import com.merge.final_project.global.exceptions.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;

    //알림 전송 ( 전송 시 데이터 insert)
    @Override
    @Transactional
    public void send(RecipientType recipientType, Long receiverNo, NotificationType notificationType, String content) {
        Notification notification = Notification.builder()
                .recipientType(recipientType)
                .receiverNo(receiverNo)
                .notificationType(notificationType)
                .content(content)
                .isRead(false)
                .build();
        //save 하니까 트랜잭션 추가함.
        notificationRepository.save(notification);
    }

    // 수행 대상 별로 본인 알림 읽어오기
    @Override
    public Page<NotificationResponseDTO> getNotifications(RecipientType recipientType, Long receiverNo, Pageable pageable) {
        return notificationRepository
                .findByReceiverNoAndRecipientType(receiverNo, recipientType, pageable)
                .map(NotificationResponseDTO::from);
    }

    // 수행 대상 별로 안 읽은 알림 개수 조회하기
    @Override
    public long getUnreadCount(RecipientType recipientType, Long receiverNo) {
        return notificationRepository
                .countByReceiverNoAndRecipientTypeAndIsRead(receiverNo, recipientType, false);
    }

    //알림 읽음 처리하기 (상태 수정) - [가빈] 소유권 검증 추가 (IDOR 방지)
    @Override
    @Transactional
    public void markAsRead(Long notificationNo, RecipientType recipientType, Long receiverNo) {
        Notification notification = notificationRepository.findById(notificationNo)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOTIFICATION_NOT_FOUND));

        // 해당 알림이 현재 로그인한 사용자의 것인지 검증
        if (!notification.getReceiverNo().equals(receiverNo)
                || notification.getRecipientType() != recipientType) {
            throw new BusinessException(ErrorCode.NOTIFICATION_ACCESS_DENIED);
        }

        if (!notification.isRead()) {
            notification.markAsRead();
        }
    }

    //전체 알람 읽음 처리하기
    @Override
    @Transactional
    public void markAllAsRead(RecipientType recipientType, Long receiverNo) {
        notificationRepository
                .findByReceiverNoAndRecipientType(receiverNo, recipientType, Pageable.unpaged())
                .filter(n -> !n.isRead())
                .forEach(Notification::markAsRead);
    }
}

package com.merge.final_project.admin.service;

import com.merge.final_project.notification.inapp.Notification;
import com.merge.final_project.notification.inapp.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
public class NotificationBatchSaver {

    private final NotificationRepository notificationRepository;

    // 호출할 때마다 독립 트랜잭션 — 청크 단위로 커밋해 긴 트랜잭션·OOM 방지
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveAll(List<Notification> notifications) {
        notificationRepository.saveAll(notifications);
    }
}

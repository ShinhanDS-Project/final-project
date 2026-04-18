package com.merge.final_project.admin.service;

import com.merge.final_project.notification.inapp.Notification;
import com.merge.final_project.notification.inapp.NotificationRepository;
import com.merge.final_project.notification.inapp.NotificationType;
import com.merge.final_project.notification.inapp.RecipientType;
import com.merge.final_project.org.FoundationRepository;
import com.merge.final_project.recipient.beneficiary.repository.BeneficiaryRepository;
import com.merge.final_project.user.users.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminBroadcastServiceImpl implements AdminBroadcastService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final FoundationRepository foundationRepository;
    private final BeneficiaryRepository beneficiaryRepository;

    @Override
    @Transactional
    public int broadcast(String content) {
        List<Notification> notifications = new ArrayList<>();

        // 활성 회원
        userRepository.findAllActiveUserNos().forEach(no ->
                notifications.add(build(RecipientType.USERS, no, content)));

        // 활성 기부단체 (accountStatus = ACTIVE)
        foundationRepository.findAllActiveFoundationNos().forEach(no ->
                notifications.add(build(RecipientType.FOUNDATION, no, content)));

        // 수혜자 전체 (상태 구분 없음)
        beneficiaryRepository.findAllBeneficiaryNos().forEach(no ->
                notifications.add(build(RecipientType.BENEFICIARY, no, content)));

        notificationRepository.saveAll(notifications);
        log.info("전체 공지 발송 완료 - 총 {}건", notifications.size());
        return notifications.size();
    }

    private Notification build(RecipientType recipientType, Long receiverNo, String content) {
        return Notification.builder()
                .recipientType(recipientType)
                .receiverNo(receiverNo)
                .notificationType(NotificationType.ADMIN_NOTICE)
                .content(content)
                .isRead(false)
                .build();
    }
}

package com.merge.final_project.admin.service;

import com.merge.final_project.notification.inapp.Notification;
import com.merge.final_project.notification.inapp.NotificationType;
import com.merge.final_project.notification.inapp.RecipientType;
import com.merge.final_project.org.FoundationRepository;
import com.merge.final_project.recipient.beneficiary.repository.BeneficiaryRepository;
import com.merge.final_project.user.users.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminBroadcastServiceImpl implements AdminBroadcastService {

    private static final int BATCH_SIZE = 500;

    private final NotificationBatchSaver notificationBatchSaver;
    private final UserRepository userRepository;
    private final FoundationRepository foundationRepository;
    private final BeneficiaryRepository beneficiaryRepository;

    @Override
    public int broadcast(String content) {
        int total = 0;
        total += sendInBatches(RecipientType.USERS,        userRepository.findAllActiveUserNos(),         content);
        total += sendInBatches(RecipientType.FOUNDATION,   foundationRepository.findAllActiveFoundationNos(), content);
        total += sendInBatches(RecipientType.BENEFICIARY,  beneficiaryRepository.findAllBeneficiaryNos(), content);
        log.info("전체 공지 발송 완료 - 총 {}건", total);
        return total;
    }

    private int sendInBatches(RecipientType type, List<Long> nos, String content) {
        int total = 0;
        for (int i = 0; i < nos.size(); i += BATCH_SIZE) {
            List<Long> chunk = nos.subList(i, Math.min(i + BATCH_SIZE, nos.size()));
            List<Notification> notifications = chunk.stream()
                    .map(no -> build(type, no, content))
                    .toList();
            notificationBatchSaver.saveAll(notifications);
            total += notifications.size();
        }
        return total;
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

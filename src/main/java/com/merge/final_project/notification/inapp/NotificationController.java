package com.merge.final_project.notification.inapp;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    // 알림 목록 조회 - 프론트에서 로그인 응답으로 받은 receiverNo와 recipientType을 함께 전달
    @GetMapping
    public ResponseEntity<Page<NotificationResponseDTO>> getNotifications(
            @RequestParam RecipientType recipientType,
            @RequestParam Long receiverNo,
            Pageable pageable) {
        return ResponseEntity.ok(
                notificationService.getNotifications(recipientType, receiverNo, pageable)
        );
    }

    // 읽지 않은 알림 수 (배지용)
    @GetMapping("/unread-count")
    public ResponseEntity<Long> getUnreadCount(
            @RequestParam RecipientType recipientType,
            @RequestParam Long receiverNo) {
        return ResponseEntity.ok(
                notificationService.getUnreadCount(recipientType, receiverNo)
        );
    }

    // 단건 읽음 처리
    @PatchMapping("/{notificationNo}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable Long notificationNo) {
        notificationService.markAsRead(notificationNo);
        return ResponseEntity.ok().build();
    }

    // 전체 읽음 처리
    @PatchMapping("/read-all")
    public ResponseEntity<Void> markAllAsRead(
            @RequestParam RecipientType recipientType,
            @RequestParam Long receiverNo) {
        notificationService.markAllAsRead(recipientType, receiverNo);
        return ResponseEntity.ok().build();
    }
}

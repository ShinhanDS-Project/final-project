package com.merge.final_project.notification.inapp;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    // 알림 목록 조회
    @GetMapping
    public ResponseEntity<Page<NotificationResponseDTO>> getNotifications(
            @PageableDefault(sort="createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        //수행 대상 PK와 수행대상 테이블 명을 토큰에서 추출해내기 위함.
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Long receiverNo = (Long) auth.getDetails();
        RecipientType recipientType = RecipientType.from(auth.getAuthorities().iterator().next().getAuthority());

        return ResponseEntity.ok(notificationService.getNotifications(recipientType, receiverNo, pageable));
    }

    // 읽지 않은 알림 수 (배지용)
    @GetMapping("/unread-count")
    public ResponseEntity<Long> getUnreadCount() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Long receiverNo = (Long) auth.getDetails();
        RecipientType recipientType = RecipientType.from(auth.getAuthorities().iterator().next().getAuthority());

        return ResponseEntity.ok(notificationService.getUnreadCount(recipientType, receiverNo));
    }

    // 단건 읽음 처리 =>JWT에서 소유자 정보 추출하여 소유권 검증
    @PatchMapping("/{notificationNo}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable Long notificationNo) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        RecipientType recipientType = RecipientType.from(auth.getAuthorities().iterator().next().getAuthority());
        notificationService.markAsRead(notificationNo, recipientType, (Long) auth.getDetails());
        return ResponseEntity.ok().build();
    }

    // 전체 읽음 처리
    @PatchMapping("/read-all")
    public ResponseEntity<Void> markAllAsRead() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Long receiverNo = (Long) auth.getDetails();
        RecipientType recipientType = RecipientType.from(auth.getAuthorities().iterator().next().getAuthority());

        notificationService.markAllAsRead(recipientType, receiverNo);
        return ResponseEntity.ok().build();
    }
}

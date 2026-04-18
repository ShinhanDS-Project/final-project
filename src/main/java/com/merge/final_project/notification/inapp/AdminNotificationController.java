package com.merge.final_project.notification.inapp;

import com.merge.final_project.admin.dto.AdminBroadcastRequestDTO;
import com.merge.final_project.admin.service.AdminBroadcastService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/admin/notifications")
@RequiredArgsConstructor
public class AdminNotificationController {

    private final NotificationService notificationService;
    private final AdminBroadcastService adminBroadcastService;

    /**
     * GET /admin/notifications
     * 전체 알림 내역 조회 (필터링 + 검색 + 페이징 + 정렬)
     *
     * @param recipientType    USERS | FOUNDATION | BENEFICIARY (미입력 시 전체)
     * @param notificationType NotificationType enum 값 (미입력 시 전체)
     * @param isRead           true | false (미입력 시 전체)
     * @param keyword          content 검색 (대소문자 무관)
     * @param from             조회 시작일 yyyy-MM-dd
     * @param to               조회 종료일 yyyy-MM-dd (해당 일 포함)
     * @param pageable         page, size, sort (DB 컬럼명: created_at, notification_no 등)
     */
    @GetMapping
    public ResponseEntity<Page<AdminNotificationResponseDTO>> getNotifications(
            @RequestParam(required = false) RecipientType recipientType,
            @RequestParam(required = false) NotificationType notificationType,
            @RequestParam(required = false) Boolean isRead,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @PageableDefault(size = 20, sort = "created_at", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(
                notificationService.getAll(recipientType, notificationType, isRead, keyword, from, to, pageable)
        );
    }

    /**
     * POST /admin/notifications/broadcast
     * 전체 공지 발송 (활성 회원 + 활성 기부단체 + 전체 수혜자)
     *
     * @return 발송된 알림 총 건수
     */
    @PostMapping("/broadcast")
    public ResponseEntity<Map<String, Integer>> broadcast(@RequestBody AdminBroadcastRequestDTO request) {
        int count = adminBroadcastService.broadcast(request.getContent());
        return ResponseEntity.ok(Map.of("sent", count));
    }
}

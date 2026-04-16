package com.merge.final_project.notification.inapp;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@Tag(name = "인앱 알림", description = "사용자·기부단체·수혜자 인앱 알림 조회·읽음 처리 API")
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @Operation(summary = "알림 목록 조회", description = "로그인한 사용자(일반/기부단체/수혜자)의 알림 목록을 최신순으로 페이징 조회합니다. JWT 토큰에서 수신자 정보를 자동 추출합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 요청")
    })
    @GetMapping
    public ResponseEntity<Page<NotificationResponseDTO>> getNotifications(
            @PageableDefault(sort="created_at", direction = Sort.Direction.DESC) Pageable pageable) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Long receiverNo = (Long) auth.getDetails();
        RecipientType recipientType = RecipientType.from(auth.getAuthorities().iterator().next().getAuthority());

        return ResponseEntity.ok(notificationService.getNotifications(recipientType, receiverNo, pageable));
    }

    @Operation(summary = "읽지 않은 알림 수 조회", description = "로그인한 사용자의 읽지 않은 알림 수를 반환합니다. 상단 배지(badge) 표시용입니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 요청")
    })
    @GetMapping("/unread-count")
    public ResponseEntity<Long> getUnreadCount() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Long receiverNo = (Long) auth.getDetails();
        RecipientType recipientType = RecipientType.from(auth.getAuthorities().iterator().next().getAuthority());

        return ResponseEntity.ok(notificationService.getUnreadCount(recipientType, receiverNo));
    }

    @Operation(summary = "단건 읽음 처리", description = "지정한 알림을 읽음으로 처리합니다. JWT에서 소유자 정보를 추출하여 소유권을 검증합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "읽음 처리 성공"),
            @ApiResponse(responseCode = "403", description = "본인 알림이 아님"),
            @ApiResponse(responseCode = "404", description = "알림을 찾을 수 없음"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 요청")
    })
    @PatchMapping("/{notificationNo}/read")
    public ResponseEntity<Void> markAsRead(
            @Parameter(description = "알림 번호", example = "1") @PathVariable Long notificationNo) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        RecipientType recipientType = RecipientType.from(auth.getAuthorities().iterator().next().getAuthority());
        notificationService.markAsRead(notificationNo, recipientType, (Long) auth.getDetails());
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "전체 읽음 처리", description = "로그인한 사용자의 모든 미읽음 알림을 한 번에 읽음으로 처리합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "전체 읽음 처리 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 요청")
    })
    @PatchMapping("/read-all")
    public ResponseEntity<Void> markAllAsRead() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Long receiverNo = (Long) auth.getDetails();
        RecipientType recipientType = RecipientType.from(auth.getAuthorities().iterator().next().getAuthority());

        notificationService.markAllAsRead(recipientType, receiverNo);
        return ResponseEntity.ok().build();
    }
}

package com.merge.final_project.admin.sse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.UUID;

@Tag(name = "관리자 SSE 실시간 알림", description = "관리자 대시보드 실시간 승인 요청 알림 SSE API")
@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminSseController {

    private final SseEmitterService sseEmitterService;

    @Operation(
            summary = "관리자 SSE 구독",
            description = "관리자 대시보드 진입 시 SSE 연결을 맺습니다. 캠페인·기부단체 승인 요청 이벤트를 실시간으로 수신합니다. "
                    + "여러 탭 동시 접속을 지원합니다. Content-Type: text/event-stream"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "SSE 연결 성공 (스트림 유지)"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 요청"),
            @ApiResponse(responseCode = "403", description = "관리자 권한 없음")
    })
    @GetMapping(value = "/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribe(@AuthenticationPrincipal String adminId) {
        String emitterId = adminId + "_" + UUID.randomUUID();
        return sseEmitterService.subscribe(emitterId);
    }
}

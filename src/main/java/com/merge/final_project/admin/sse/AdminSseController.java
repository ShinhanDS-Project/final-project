package com.merge.final_project.admin.sse;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.UUID;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminSseController {

    private final SseEmitterService sseEmitterService;

    // 관리자 대시보드/공통 레이아웃 진입 시 SSE 연결하는 컨트롤러
    // adminId에 UUID 더해서 emitterId설정 => 여러 탭 동시 접속 지원
    @GetMapping(value = "/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE) //sse 사용하니까 content-type을 text/event-stream으로 설정
    public SseEmitter subscribe(@AuthenticationPrincipal String adminId) {
        String emitterId = adminId + "_" + UUID.randomUUID();
        return sseEmitterService.subscribe(emitterId);
    }
}

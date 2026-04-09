package com.merge.final_project.admin.sse;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class ApprovalEventListener {

    //SseEmitter 객체 생성 서비스 주입
    private final SseEmitterService sseEmitterService;

    // 각 메서드에서 트랜잭션 커밋 이후에만 SSE 브로드캐스트 (롤백된 데이터 전송 방지)
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onApprovalRequest(ApprovalRequestEvent event) {
        sseEmitterService.broadcast(event);
    }
}

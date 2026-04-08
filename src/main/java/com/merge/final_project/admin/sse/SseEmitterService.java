package com.merge.final_project.admin.sse;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class SseEmitterService {
    //현재 sse에 접속한 관리자들을 담아 놓을 map
    private final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();

    // emitterId = adminNo_UUID 형식. => 한 관리자가 여러 탭에서 접속할 수도 있으니 사용자 연결을 식별하는 고유 ID 부여
    // 각 접속자들을 연결시키는 작업.
    public SseEmitter subscribe(String emitterId) {
        //인자값으로 타임타웃 시간을 주고 SseEmitter 객체 생성.
        SseEmitter emitter = new SseEmitter(30 * 60 * 1000L);   //30분 설정

        //구독 종료, 타임아웃, 에러가 발생하면 서버 메모리(Map)에서 emitter를 제거한다.
        emitter.onCompletion(() -> {
            emitters.remove(emitterId);
            log.debug("SSE 연결 종료: {}", emitterId);
        });
        emitter.onTimeout(() -> {
            emitters.remove(emitterId);
            log.debug("SSE 타임아웃: {}", emitterId);
        });
        emitter.onError(e -> {
            emitters.remove(emitterId);
            log.debug("SSE 에러: {}", emitterId);
        });

        //접속자 고유의 id와 객체를 서버 메모리 맵에 추가한다.
        emitters.put(emitterId, emitter);

        // 연결 직후 초기 이벤트 전송 (503 방지)
        try {
            //해당 접속자에게 connect라는 이벤트 이름으로 data 전송
            emitter.send(SseEmitter.event().name("connect").data("connected"));
        } catch (IOException e) {
            emitters.remove(emitterId);
        }

        return emitter;
    }

    // 연결된 모든 관리자에게 이벤트 브로드캐스트
    public void broadcast(ApprovalRequestEvent event) {
        //forEach로 map 순회하면서 고유 id와 emitter 객체마다 이벤트 전송.
        emitters.forEach((id, emitter) -> {
            try {
                emitter.send(SseEmitter.event()
                        .name("approval-request")
                        .data(event));
            } catch (IOException e) {
                emitters.remove(id);
                log.debug("브로드캐스트 실패, emitter 제거: {}", id);
            }
        });
    }

    // 추후 프록시나 로드밸런서가 아무 데이터 없는 연결을 끊는 걸 방지하기 위해 30초마다 heartbeat 전송
    @Scheduled(fixedDelay = 30000)
    public void sendHeartbeat() {
        emitters.forEach((id, emitter) -> {
            try {
                emitter.send(SseEmitter.event().name("ping").data("keep-alive"));
            } catch (IOException e) {
                emitters.remove(id);
            }
        });
    }
}

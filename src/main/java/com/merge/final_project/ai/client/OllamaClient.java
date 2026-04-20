package com.merge.final_project.ai.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * [AI 추천] 로컬 Ollama (Gemma3) 구현체
 */
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "ai.type", havingValue = "ollama")
public class OllamaClient implements AiClient {

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String generateRecommendationReason(String donorType, String userSummary, String campaignTitle, String campaignCategory) {
        String url = "http://localhost:11434/api/chat";

        String prompt = String.format(
                "사용자 정보: %s, %s\n추천 캠페인: [%s] \"%s\"\n\n" +
                "기부 독려 AI로서 1~2문장의 따뜻한 한국어 추천 사유를 작성해줘. 해요체 사용.",
                donorType, userSummary, campaignCategory, campaignTitle
        );

        Map<String, Object> body = new HashMap<>();
        body.put("model", "gemmafour:latest");
        body.put("stream", false);
        body.put("messages", List.of(Map.of("role", "user", "content", prompt)));

        try {
            Map<String, Object> resp = restTemplate.postForObject(url, new HttpEntity<>(body), Map.class);
            return (String) ((Map<String, Object>) resp.get("message")).get("content");
        } catch (Exception e) {
            return "로컬 AI 추천: 세상을 따뜻하게 만드는 기부에 동참하세요!";
        }
    }

    @Override
    public Map<String, Object> analyzeSearchIntent(String userQuery) {
        String url = "http://localhost:11434/api/chat";
        String prompt = String.format("의도: \"%s\"\n\n카테고리(CHILD_YOUTH, SENIOR, DISABLED, ANIMAL, ENVIRONMENT, ETC) 중 하나와 키워드 3개를 JSON으로만 응답해.", userQuery);

        Map<String, Object> body = new HashMap<>();
        body.put("model", "gemmafour:latest");
        body.put("stream", false);
        body.put("messages", List.of(Map.of("role", "user", "content", prompt)));

        try {
            Map<String, Object> resp = restTemplate.postForObject(url, new HttpEntity<>(body), Map.class);
            String content = (String) ((Map<String, Object>) resp.get("message")).get("content");
            content = content.replaceAll("```json|```", "").trim();
            return objectMapper.readValue(content, Map.class);
        } catch (Exception e) {
            return Map.of("category", "ETC", "keywords", List.of());
        }
    }

    @Override
    public String chatTest(String userMessage) {
        String url = "http://localhost:11434/api/chat";

        Map<String, Object> body = new HashMap<>();
        body.put("model", "gemmafour:latest");
        body.put("stream", false);
        body.put("messages", List.of(
                Map.of("role", "system", "content", "당신은 로컬에서 실행 중인 친절한 기부 천사 Gemma3 AI입니다. 질문에 아주 정성껏 대답해줘."),
                Map.of("role", "user", "content", userMessage)
        ));

        try {
            Map<String, Object> resp = restTemplate.postForObject(url, new HttpEntity<>(body), Map.class);
            if (resp != null && resp.containsKey("message")) {
                return (String) ((Map<String, Object>) resp.get("message")).get("content");
            }
        } catch (Exception e) {
            return "Gemma3 호출 에러: " + e.getMessage();
        }
        return "Gemma3로부터 응답을 받지 못했습니다.";
    }

    @Override
    public void streamChat(String userMessage, org.springframework.web.servlet.mvc.method.annotation.SseEmitter emitter) {
        String url = "http://localhost:11434/api/chat";

        Map<String, Object> body = new HashMap<>();
        body.put("model", "gemmafour:latest");
        body.put("stream", true);
        body.put("messages", List.of(
                Map.of("role", "system", "content", "당신은 로컬에서 실행 중인 친절한 기부 천사 Gemma3 AI입니다. 질문에 아주 정성껏 대답해줘."),
                Map.of("role", "user", "content", userMessage)
        ));

        restTemplate.execute(url, org.springframework.http.HttpMethod.POST, request -> {
            request.getHeaders().setContentType(MediaType.APPLICATION_JSON);
            objectMapper.writeValue(request.getBody(), body);
        }, response -> {
            try (java.io.BufferedReader reader = new java.io.BufferedReader(
                    new java.io.InputStreamReader(response.getBody(), java.nio.charset.StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.trim().isEmpty()) continue;
                    try {
                        Map<String, Object> map = objectMapper.readValue(line, Map.class);
                        if (map.containsKey("message")) {
                            Map<String, Object> message = (Map<String, Object>) map.get("message");
                            if (message != null && message.containsKey("content")) {
                                String content = (String) message.get("content");
                                if (content != null && !content.isEmpty()) {
                                    emitter.send(org.springframework.web.servlet.mvc.method.annotation.SseEmitter.event().data(content));
                                }
                            }
                        }
                        if (Boolean.TRUE.equals(map.get("done"))) {
                            break;
                        }
                    } catch (Exception e) {
                        // 개별 라인 파싱 에러 무시
                    }
                }
                emitter.complete();
            } catch (Exception e) {
                emitter.completeWithError(e);
            }
            return null;
        });
    }

    @Override
    public String checkApiConfig() {
        return "✅ 로컬 Ollama(Gemma3) 모드 작동 중";
    }
}

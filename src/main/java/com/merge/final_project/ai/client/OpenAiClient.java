package com.merge.final_project.ai.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * [AI 추천] OpenAI (GPT-4o-mini) 구현체
 */
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "ai.type", havingValue = "openai", matchIfMissing = true)
public class OpenAiClient implements AiClient {

    @Value("${openapi.service.key}")
    private String serviceKey;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String generateRecommendationReason(String donorType, String userSummary, String campaignTitle, String campaignCategory) {
        String url = "https://api.openai.com/v1/chat/completions";

        String prompt = String.format(
                "사용자 성향: %s, %s\n추천된 캠페인: [%s] \"%s\"\n\n" +
                "당신은 따뜻하고 친절한 기부 컨설턴트입니다. 위 사용자의 기부 패턴과 캠페인의 특성을 연결 지어, " +
                "왜 이 캠페인을 추천하는지 1~2문장의 정중한 존댓말(해요체)로 작성해 주세요. " +
                "단순히 '동참하세요' 보다는 사용자의 가치를 높여주는 따뜻한 문구로 부탁드립니다.",
                donorType, userSummary, campaignCategory, campaignTitle
        );

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "gpt-4o-mini");
        requestBody.put("messages", List.of(
                Map.of("role", "system", "content", "친절하고 정중한 기부 상담사"),
                Map.of("role", "user", "content", prompt)
        ));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(serviceKey);

        try {
            Map<String, Object> resp = restTemplate.postForObject(url, new HttpEntity<>(requestBody, headers), Map.class);
            List<Map<String, Object>> choices = (List<Map<String, Object>>) resp.get("choices");
            return (String) ((Map<String, Object>) choices.get(0).get("message")).get("content");
        } catch (Exception e) {
            return "회원님의 따뜻한 마음이 필요한 캠페인입니다. 함께 세상을 밝혀주세요!";
        }
    }

    @Override
    public Map<String, Object> analyzeSearchIntent(String userQuery) {
        String url = "https://api.openai.com/v1/chat/completions";
        String prompt = String.format(
                "사용자 질문: \"%s\"\n\n" +
                "다음 정보를 JSON으로 응답해줘:\n" +
                "1. category: (CHILD_YOUTH, SENIOR, DISABLED, ANIMAL, ENVIRONMENT, ETC) 중 하나\n" +
                "2. keywords: 검색용 핵심 키워드 3개 (배열)\n" +
                "3. sort: (LOW_ACHIEVEMENT, HIGH_ACHIEVEMENT, ENDING_SOON, NEWEST, DEFAULT) 중 하나\n" +
                "   - '기부 미비', '도움이 필요한' -> LOW_ACHIEVEMENT\n" +
                "   - '인기 있는', '거의 다 된' -> HIGH_ACHIEVEMENT\n" +
                "   - '마감 임박', '빨리 끝나는' -> ENDING_SOON\n" +
                "   - '최신', '방금 올라온' -> NEWEST", 
                userQuery);

        Map<String, Object> body = new HashMap<>();
        body.put("model", "gpt-4o-mini");
        body.put("response_format", Map.of("type", "json_object"));
        body.put("messages", List.of(Map.of("role", "user", "content", prompt)));

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(serviceKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        try {
            Map<String, Object> resp = restTemplate.postForObject(url, new HttpEntity<>(body, headers), Map.class);
            String content = (String) ((Map<String, Object>) ((List<Map<String, Object>>) resp.get("choices")).get(0).get("message")).get("content");
            return objectMapper.readValue(content, Map.class);
        } catch (Exception e) {
            return Map.of("category", "ETC", "keywords", List.of(), "sort", "DEFAULT");
        }
    }

    @Override
    public String chatTest(String userMessage) {
        String url = "https://api.openai.com/v1/chat/completions";

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "gpt-4o-mini");
        requestBody.put("messages", List.of(
                Map.of("role", "system", "content", "당신은 기부 플랫폼의 친절한 도우미 AI입니다."),
                Map.of("role", "user", "content", userMessage)
        ));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(serviceKey);

        try {
            Map<String, Object> resp = restTemplate.postForObject(url, new HttpEntity<>(requestBody, headers), Map.class);
            if (resp != null && resp.containsKey("choices")) {
                List<Map<String, Object>> choices = (List<Map<String, Object>>) resp.get("choices");
                return (String) ((Map<String, Object>) choices.get(0).get("message")).get("content");
            }
        } catch (Exception e) {
            return "OpenAI 호출 에러: " + e.getMessage();
        }
        return "OpenAI로부터 응답을 받지 못했습니다.";
    }

    @Override
    public void streamChat(String userMessage, org.springframework.web.servlet.mvc.method.annotation.SseEmitter emitter) {
        String url = "https://api.openai.com/v1/chat/completions";

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "gpt-4o-mini");
        requestBody.put("stream", true);
        requestBody.put("messages", List.of(
                Map.of("role", "system", "content", "당신은 기부 플랫폼의 친절한 도우미 AI입니다."),
                Map.of("role", "user", "content", userMessage)
        ));

        restTemplate.execute(url, org.springframework.http.HttpMethod.POST, request -> {
            request.getHeaders().setContentType(MediaType.APPLICATION_JSON);
            request.getHeaders().setBearerAuth(serviceKey);
            objectMapper.writeValue(request.getBody(), requestBody);
        }, response -> {
            try (java.io.BufferedReader reader = new java.io.BufferedReader(
                    new java.io.InputStreamReader(response.getBody(), java.nio.charset.StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.startsWith("data:") && !line.contains("[DONE]")) {
                        try {
                            String json = line.substring(5).trim();
                            if (json.isEmpty()) continue;
                            
                            Map<String, Object> map = objectMapper.readValue(json, Map.class);
                            List<Map<String, Object>> choices = (List<Map<String, Object>>) map.get("choices");
                            if (choices != null && !choices.isEmpty()) {
                                Map<String, Object> delta = (Map<String, Object>) choices.get(0).get("delta");
                                if (delta != null && delta.get("content") != null) {
                                    String content = (String) delta.get("content");
                                    if (!content.isEmpty()) {
                                        emitter.send(org.springframework.web.servlet.mvc.method.annotation.SseEmitter.event().data(content));
                                    }
                                }
                            }
                        } catch (Exception e) {
                            // JSON 파싱 에러 등은 무시
                        }
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
        return "✅ OpenAI 모드 작동 중";
    }
}

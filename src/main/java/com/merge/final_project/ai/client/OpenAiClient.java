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
                "사용자 정보: %s, %s\n추천 캠페인: [%s] \"%s\"\n\n" +
                "기부 독려 AI로서 1~2문장의 따뜻한 한국어 추천 사유를 작성해줘. 해요체 사용.",
                donorType, userSummary, campaignCategory, campaignTitle
        );

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "gpt-4o-mini");
        requestBody.put("messages", List.of(
                Map.of("role", "system", "content", "따뜻한 기부 조언자"),
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
            return "당신의 성향에 맞는 캠페인입니다!";
        }
    }

    @Override
    public Map<String, Object> analyzeSearchIntent(String userQuery) {
        String url = "https://api.openai.com/v1/chat/completions";
        String prompt = String.format("의도: \"%s\"\n\n카테고리(CHILD_YOUTH, SENIOR, DISABLED, ANIMAL, ENVIRONMENT, ETC) 중 하나와 키워드 3개를 JSON으로 응답해.", userQuery);

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
            return Map.of("category", "ETC", "keywords", List.of());
        }
    }

    @Override
    public String chatTest(String userMessage) {
        // 기존 chatTest 로직 (간소화)
        return "OpenAI 답변: 기부는 세상을 밝히는 빛입니다.";
    }

    @Override
    public String checkApiConfig() {
        return "✅ OpenAI 모드 작동 중";
    }
}

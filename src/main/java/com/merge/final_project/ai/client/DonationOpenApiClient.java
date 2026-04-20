package com.merge.final_project.ai.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * [AI 추천] 외부 OpenAPI 연동 클라이언트
 */
@Component
public class DonationOpenApiClient {

    @Value("${openapi.service.key}")
    private String serviceKey;

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * OpenAI GPT를 사용하여 맞춤형 추천 사유를 생성합니다.
     */
    public String generateRecommendationReason(String donorType, String userSummary, String campaignTitle, String campaignCategory) {
        String url = "https://api.openai.com/v1/chat/completions";

        // GPT에게 전달할 프롬프트 구성 (사용자 맞춤형 정보 강화)
        String prompt = String.format(
            "사용자 정보: %s, %s\n" +
            "추천 캠페인: [%s] 카테고리의 \"%s\"\n\n" +
            "당신은 기부자의 과거 활동을 기반으로 새로운 기부를 독려하는 친절한 AI 조언자입니다. " +
            "사용자의 과거 관심 분야나 기부 성향을 언급하며, 이 캠페인이 왜 당신에게 특별한지 1~2문장의 아주 따뜻한 한국어로 말해줘. " +
            "반드시 '해요'체나 '습니다'체 같은 따뜻한 존댓말을 사용해줘.",
            donorType, userSummary, campaignCategory, campaignTitle
        );

        // OpenAI API 요청 바디 구성 (Map 사용)
        java.util.Map<String, Object> requestBody = new java.util.HashMap<>();
        requestBody.put("model", "gpt-4o-mini"); // 가성비 모델 gpt-4o-mini로 변경
        
        java.util.List<java.util.Map<String, String>> messages = new java.util.ArrayList<>();
        messages.add(java.util.Map.of("role", "system", "content", "당신은 기부 플랫폼의 친절한 AI 조언자입니다."));
        messages.add(java.util.Map.of("role", "user", "content", prompt));
        requestBody.put("messages", messages);
        requestBody.put("max_tokens", 200);

        // HTTP 헤더 설정
        org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
        headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);
        headers.setBearerAuth(serviceKey);

        org.springframework.http.HttpEntity<java.util.Map<String, Object>> entity = new org.springframework.http.HttpEntity<>(requestBody, headers);

        try {
            // API 호출 및 결과 파싱
            java.util.Map<String, Object> response = restTemplate.postForObject(url, entity, java.util.Map.class);
            if (response != null && response.containsKey("choices")) {
                java.util.List<java.util.Map<String, Object>> choices = (java.util.List<java.util.Map<String, Object>>) response.get("choices");
                java.util.Map<String, Object> message = (java.util.Map<String, Object>) choices.get(0).get("message");
                return (String) message.get("content");
            }
        } catch (Exception e) {
            System.err.println("OpenAI API 호출 실패: " + e.getMessage());
        }

        return "당신의 따뜻한 기부 성향에 딱 맞는 캠페인입니다. 함께 세상을 바꿔보세요!";
    }

    /**
     * 사용자의 자연어 기부 의도를 분석하여 적절한 카테고리와 키워드를 추출합니다.
     */
    public java.util.Map<String, Object> analyzeSearchIntent(String userQuery) {
        String url = "https://api.openai.com/v1/chat/completions";

        String prompt = String.format(
            "사용자의 기부 의도: \"%s\"\n\n" +
            "위 문장을 분석해서 다음 카테고리 중 가장 적합한 것 하나를 골라주고, 검색에 사용할 키워드 3개를 뽑아줘.\n" +
            "카테고리 목록: CHILD_YOUTH, SENIOR, DISABLED, ANIMAL, ENVIRONMENT, ETC\n\n" +
            "응답 형식(JSON): {\"category\": \"카테고리명\", \"keywords\": [\"키워드1\", \"키워드2\", \"키워드3\"]}",
            userQuery
        );

        java.util.Map<String, Object> requestBody = new java.util.HashMap<>();
        requestBody.put("model", "gpt-4o-mini");
        requestBody.put("messages", java.util.List.of(
            java.util.Map.of("role", "system", "content", "당신은 사용자의 검색 의도를 분석하는 전문가입니다. 반드시 JSON 형식으로만 응답하세요."),
            java.util.Map.of("role", "user", "content", prompt)
        ));
        requestBody.put("response_format", java.util.Map.of("type", "json_object"));

        org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
        headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);
        headers.setBearerAuth(serviceKey);

        try {
            org.springframework.http.HttpEntity<java.util.Map<String, Object>> entity = new org.springframework.http.HttpEntity<>(requestBody, headers);
            java.util.Map<String, Object> response = restTemplate.postForObject(url, entity, java.util.Map.class);
            if (response != null && response.containsKey("choices")) {
                java.util.List<java.util.Map<String, Object>> choices = (java.util.List<java.util.Map<String, Object>>) response.get("choices");
                java.util.Map<String, Object> message = (java.util.Map<String, Object>) choices.get(0).get("message");
                String content = (String) message.get("content");
                
                // Jackson 등으로 파싱 (여기서는 단순화를 위해 가공 로직 생략하고 기본 Map 반환)
                return new com.fasterxml.jackson.databind.ObjectMapper().readValue(content, java.util.Map.class);
            }
        } catch (Exception e) {
            System.err.println("검색 의도 분석 실패: " + e.getMessage());
        }

        return java.util.Map.of("category", "ETC", "keywords", java.util.List.of());
    }

    /**
     * GPT 연동 테스트를 위한 자유 채팅 메서드입니다.
     */
    public String chatTest(String userMessage) {
        String url = "https://api.openai.com/v1/chat/completions";

        java.util.Map<String, Object> requestBody = new java.util.HashMap<>();
        requestBody.put("model", "gpt-4o-mini");
        
        java.util.List<java.util.Map<String, String>> messages = new java.util.ArrayList<>();
        messages.add(java.util.Map.of("role", "system", "content", "당신은 세상에서 가장 친절한 기부 천사 AI입니다. 기부에 관한 질문이나 일반적인 질문에 따뜻하게 대답해줍니다."));
        messages.add(java.util.Map.of("role", "user", "content", userMessage));
        requestBody.put("messages", messages);
        requestBody.put("max_tokens", 500);

        org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
        headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);
        headers.setBearerAuth(serviceKey);

        org.springframework.http.HttpEntity<java.util.Map<String, Object>> entity = new org.springframework.http.HttpEntity<>(requestBody, headers);

        try {
            java.util.Map<String, Object> response = restTemplate.postForObject(url, entity, java.util.Map.class);
            if (response != null && response.containsKey("choices")) {
                java.util.List<java.util.Map<String, Object>> choices = (java.util.List<java.util.Map<String, Object>>) response.get("choices");
                java.util.Map<String, Object> message = (java.util.Map<String, Object>) choices.get(0).get("message");
                return (String) message.get("content");
            }
        } catch (Exception e) {
            return "GPT 연동 에러: " + e.getMessage();
        }

        return "GPT로부터 응답을 받지 못했습니다.";
    }

    /**
     * OpenAPI 키 설정을 확인합니다.
     */
    public String checkApiConfig() {
        if (serviceKey == null || serviceKey.isEmpty() || serviceKey.startsWith("${")) {
            return "❌ API 키가 설정되지 않았습니다. .env 또는 application.properties를 확인하세요.";
        }
        return "✅ OpenAPI 키 로드 완료: " + serviceKey.substring(0, 7) + "**********";
    }

    /**
     * OpenAPI를 통해 전체 기부 리스트나 외부 기부 이력을 가져오는 메서드 (뼈대)
     */
    public void fetchExternalDonationData() {
        // 예시 URL (실제 OpenAPI URL로 변경 필요)
        String url = "https://api.odcloud.kr/api/external-donations?serviceKey=" + serviceKey;
        
        // TODO: 실제 API 규격에 맞춰 DTO 매핑 및 호출 로직 구현
        // Object response = restTemplate.getForObject(url, Object.class);
        System.out.println("OpenAPI 호출 준비 완료. 키: " + serviceKey.substring(0, 5) + "...");
    }
}

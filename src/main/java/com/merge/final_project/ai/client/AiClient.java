package com.merge.final_project.ai.client;

import java.util.Map;

/**
 * [AI 추천] 다양한 AI 엔진(OpenAI, Ollama 등)을 지원하기 위한 공통 인터페이스
 */
public interface AiClient {
    String generateRecommendationReason(String donorType, String userSummary, String campaignTitle, String campaignCategory);
    Map<String, Object> analyzeSearchIntent(String userQuery);
    String chatTest(String userMessage);
    String checkApiConfig();
}

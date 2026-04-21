package com.merge.final_project.ai.controller;

import com.merge.final_project.ai.dto.RecommendedCampaignResponse;
import com.merge.final_project.ai.service.RecommendationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * [AI 추천] 캠페인 추천 API 컨트롤러
 */
@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class RecommendationController {

    private final RecommendationService recommendationService;
    private final com.merge.final_project.ai.client.AiClient aiClient;
    private final com.merge.final_project.ai.service.ChatBot chatBot;

    /**
     * API 연결 상태를 테스트합니다.
     */

    @GetMapping("/test-api")
    public ResponseEntity<String> testApiConnection() {
        return ResponseEntity.ok(aiClient.checkApiConfig());
    }

    /**
     * AI 엔진과 지능적으로 대화합니다. (LangChain RAG 적용)
     * GET /api/ai/chat?q=질문내용
     */
    @GetMapping(value = "/chat", produces = "application/json;charset=UTF-8")
    public ResponseEntity<String> chat(@org.springframework.web.bind.annotation.RequestParam("q") String question) {
        return ResponseEntity.ok()
                .header(org.springframework.http.HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate")
                .body(chatBot.chat(question));
    }

    /**
     * 사용자의 기부 이유(자연어)를 기반으로 최적의 캠페인을 검색합니다.
     * GET /api/ai/search?reason=내가 하고싶은 이유
     */
    @GetMapping("/search")
    public ResponseEntity<List<RecommendedCampaignResponse>> search(@org.springframework.web.bind.annotation.RequestParam("reason") String reason) {
        List<RecommendedCampaignResponse> results = recommendationService.searchCampaignsByReason(reason);
        return ResponseEntity.ok(results);
    }

    /**
     * 로그인된 사용자의 기부 패턴을 분석하여 맞춤형 캠페인 3개를 추천합니다.
     * GET /api/ai/recommendations
     */
    @GetMapping("/recommendations")
    public ResponseEntity<List<RecommendedCampaignResponse>> getRecommendations(org.springframework.security.core.Authentication authentication) {
        if (authentication == null || authentication.getDetails() == null) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.UNAUTHORIZED).build();
        }

        try {
            // JwtFilter의 52번 라인(authentication.setDetails(pk))에서 저장한 PK를 꺼내옵니다.
            Long userNo = (Long) authentication.getDetails();
            
            List<RecommendedCampaignResponse> recommendations = recommendationService.getRecommendedCampaigns(userNo);
            return ResponseEntity.ok(recommendations);
        } catch (Exception e) {
            // PK를 꺼내는 과정에서 문제가 생기면 401 반환
            return ResponseEntity.status(org.springframework.http.HttpStatus.UNAUTHORIZED).build();
        }
    }

    /**
     * [관리자용/테스트용] 특정 사용자의 번호로 추천 내역을 조회합니다.
     * GET /api/ai/recommendations/{userNo}
     */
    @GetMapping("/recommendations/{userNo}")
    public ResponseEntity<List<RecommendedCampaignResponse>> getRecommendations(@PathVariable("userNo") Long userNo) {
        List<RecommendedCampaignResponse> recommendations = recommendationService.getRecommendedCampaigns(userNo);
        return ResponseEntity.ok(recommendations);
    }
}

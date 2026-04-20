package com.merge.final_project.ai.service;

import com.merge.final_project.ai.dto.RecommendedCampaignResponse;
import java.util.List;

/**
 * [AI 추천] 기부 패턴 분석 및 캠페인 추천 서비스 인터페이스
 */
public interface RecommendationService {
    /**
     * 특정 사용자에게 최적화된 캠페인 리스트(3개)를 추천합니다.
     */
    List<RecommendedCampaignResponse> getRecommendedCampaigns(Long userNo);

    /**
     * 사용자의 자연어 이유(reason)를 분석하여 가장 적합한 캠페인을 검색합니다.
     */
    List<RecommendedCampaignResponse> searchCampaignsByReason(String reason);
}

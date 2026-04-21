package com.merge.final_project.ai.service;

import com.merge.final_project.campaign.campaigns.CampaignStatus;
import com.merge.final_project.campaign.campaigns.entity.Campaign;
import com.merge.final_project.campaign.campaigns.repository.CampaignRepository;
import dev.langchain4j.agent.tool.Tool;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * AI가 실시간으로 캠페인 정보를 검색할 수 있게 해주는 도구(Tool)입니다.
 * RAG 패턴에서 '정보 검색' 역할을 수행합니다.
 */
@Component
@RequiredArgsConstructor
public class CampaignRetriever {

    private final CampaignRepository campaignRepository;

    @Tool("현재 모집 중인 모든 캠페인 목록을 조회합니다.")
    public List<String> getAllActiveCampaigns() {
        return campaignRepository.findByCampaignStatus(CampaignStatus.ACTIVE).stream()
                .map(c -> String.format("[%s] %s: %s (달성률: %s%%)",
                        c.getCategory().getLabel(),
                        c.getTitle(),
                        c.getDescription(),
                        c.getCurrentAmount().divide(java.math.BigDecimal.valueOf(c.getTargetAmount()), 2, java.math.RoundingMode.HALF_UP).multiply(java.math.BigDecimal.valueOf(100))))
                .collect(Collectors.toList());
    }

    @Tool("특정 키워드가 포함된 캠페인을 검색합니다.")
    public List<String> searchCampaigns(String keyword) {
        return campaignRepository.findByCampaignStatus(CampaignStatus.ACTIVE).stream()
                .filter(c -> c.getTitle().contains(keyword) || c.getDescription().contains(keyword))
                .map(c -> String.format("[%s] %s: %s", c.getCategory().getLabel(), c.getTitle(), c.getDescription()))
                .collect(Collectors.toList());
    }
}

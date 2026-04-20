package com.merge.final_project.ai.service;

import com.merge.final_project.ai.dto.RecommendedCampaignResponse;
import com.merge.final_project.ai.dto.UserPatternDTO;
import com.merge.final_project.campaign.campaigns.CampaignStatus;
import com.merge.final_project.campaign.campaigns.entity.Campaign;
import com.merge.final_project.campaign.campaigns.repository.CampaignRepository;
import com.merge.final_project.donation.donations.Donation;
import com.merge.final_project.donation.donations.DonationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * [AI 추천] 기부 패턴 기반 캠페인 추천 서비스 구현체
 */
@Service
@RequiredArgsConstructor
public class RecommendationServiceImpl implements RecommendationService {

    private final DonationRepository donationRepository;
    private final CampaignRepository campaignRepository;
    private final com.merge.final_project.ai.client.AiClient aiClient;

    @Override
    public List<RecommendedCampaignResponse> getRecommendedCampaigns(Long userNo) {
        // 1. 사용자의 기부 패턴 분석 (금액 + 관심 카테고리)
        UserPatternDTO userPattern = analyzeUserPattern(userNo);

        // 2. 현재 모집 중인 캠페인 가져오기
        List<Campaign> activeCampaigns = campaignRepository.findByCampaignStatus(CampaignStatus.ACTIVE);

        // 3. 패턴별 맞춤 스코어링 및 추천 후보 선정
        return activeCampaigns.stream()
                .map(campaign -> scoreAndMap(campaign, userPattern))
                // 관심 카테고리 일치 여부와 달성률 등을 종합하여 정렬 (여기서는 단순화를 위해 관심 카테고리 매칭을 최우선)
                .sorted((c1, c2) -> {
                    boolean match1 = c1.getCategory().equals(userPattern.getFavoriteCategory());
                    boolean match2 = c2.getCategory().equals(userPattern.getFavoriteCategory());
                    if (match1 && !match2) return -1;
                    if (!match1 && match2) return 1;
                    return Double.compare(c2.getAchievementRate(), c1.getAchievementRate());
                })
                .limit(3)
                .peek(candidate -> {
                    // 4. AI를 이용해 각 후보별 맞춤 추천 사유 생성
                    String reasonText = aiClient.generateRecommendationReason(
                        userPattern.getDonorType().name(),
                        String.format("평균 기부액: %s, 관심 분야: %s", 
                            userPattern.getAverageDonationAmount(), 
                            userPattern.getFavoriteCategory() != null ? userPattern.getFavoriteCategory().getLabel() : "없음"),
                        candidate.getTitle(),
                        candidate.getCategory().getLabel()
                    );
                    candidate.setRecommendationReason(reasonText);
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<RecommendedCampaignResponse> searchCampaignsByReason(String reason) {
        // 1. AI를 통해 사용자의 의도 분석 (카테고리, 키워드 추출)
        java.util.Map<String, Object> intent = aiClient.analyzeSearchIntent(reason);
        String categoryStr = (String) intent.get("category");
        final List<String> keywords = (intent.get("keywords") instanceof List) ? (List<String>) intent.get("keywords") : java.util.Collections.emptyList();

        // 람다에서 사용하기 위해 final 변수로 선언 및 할당
        final com.merge.final_project.campaign.campaigns.CampaignCategory targetCategory;
        try {
            targetCategory = (categoryStr != null) ? com.merge.final_project.campaign.campaigns.CampaignCategory.valueOf(categoryStr) : com.merge.final_project.campaign.campaigns.CampaignCategory.ETC;
        } catch (Exception e) {
            return java.util.Collections.emptyList(); // 잘못된 카테고리 응답 시 빈 리스트 반환
        }

        // 2. 해당 카테고리의 ACTIVE 캠페인들 가져오기
        List<Campaign> campaigns = campaignRepository.findByCampaignStatus(CampaignStatus.ACTIVE);

        // 3. 키워드 매칭 및 스코어링
        return campaigns.stream()
                .filter(c -> c.getCategory().equals(targetCategory) || keywords.stream().anyMatch(k -> (c.getTitle() != null && c.getTitle().contains(k)) || (c.getDescription() != null && c.getDescription().contains(k))))
                .map(c -> {
                    RecommendedCampaignResponse resp = scoreAndMap(c, UserPatternDTO.builder().donorType(UserPatternDTO.DonorType.NEWBIE).build());
                    // AI에게 이 캠페인이 왜 사용자의 '이유'에 부합하는지 설명 요청
                    String customReason = aiClient.generateRecommendationReason(
                        "SEARCH_USER", 
                        "검색 의도: " + reason, 
                        c.getTitle(), 
                        c.getCategory().getLabel()
                    );
                    resp.setRecommendationReason(customReason);
                    return resp;
                })
                .limit(3)
                .collect(Collectors.toList());
    }

    /**
     * 사용자의 과거 기부 이력을 분석하여 유형 및 관심 분야를 정의합니다.
     */
    private UserPatternDTO analyzeUserPattern(Long userNo) {
        List<Donation> userDonations = donationRepository.findByUserNo(userNo);

        if (userDonations.isEmpty()) {
            return UserPatternDTO.builder()
                    .userNo(userNo)
                    .donorType(UserPatternDTO.DonorType.NEWBIE)
                    .build();
        }

        // 관심 카테고리 분석
        List<Long> pastCampaignNos = userDonations.stream().map(Donation::getCampaignNo).collect(Collectors.toList());
        List<Campaign> pastCampaigns = campaignRepository.findAllByCampaignNoIn(pastCampaignNos);
        
        com.merge.final_project.campaign.campaigns.CampaignCategory topCategory = pastCampaigns.stream()
                .collect(Collectors.groupingBy(Campaign::getCategory, Collectors.counting()))
                .entrySet().stream()
                .max(java.util.Map.Entry.comparingByValue())
                .map(java.util.Map.Entry::getKey)
                .orElse(null);

        BigDecimal totalAmount = userDonations.stream()
                .map(Donation::getDonationAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal averageAmount = totalAmount.divide(BigDecimal.valueOf(userDonations.size()), RoundingMode.HALF_UP);

        UserPatternDTO.DonorType type;
        if (userDonations.size() >= 10) {
            type = UserPatternDTO.DonorType.FREQUENT_DONOR;
        } else if (averageAmount.compareTo(new BigDecimal("100000")) >= 0) {
            type = UserPatternDTO.DonorType.LARGE_DONOR;
        } else if (averageAmount.compareTo(new BigDecimal("10000")) < 0) {
            type = UserPatternDTO.DonorType.SMALL_DONOR;
        } else {
            type = UserPatternDTO.DonorType.NEWBIE;
        }

        return UserPatternDTO.builder()
                .userNo(userNo)
                .averageDonationAmount(averageAmount)
                .totalDonationCount(userDonations.size())
                .favoriteCategory(topCategory)
                .donorType(type)
                .build();
    }

    /**
     * 캠페인과 사용자 패턴을 매칭하여 추천 DTO로 변환합니다.
     */
    private RecommendedCampaignResponse scoreAndMap(Campaign campaign, UserPatternDTO userPattern) {
        double rate = 0.0;
        if (campaign.getTargetAmount() > 0) {
            rate = campaign.getCurrentAmount()
                    .divide(BigDecimal.valueOf(campaign.getTargetAmount()), 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .doubleValue();
        }

        String reason = "당신의 기부 성향에 맞는 캠페인을 추천합니다!";

        // 패턴별 맞춤형 추천 메시지 생성
        switch (userPattern.getDonorType()) {
            case SMALL_DONOR:
                if (rate < 30) reason = "티끌 모아 태산! 당신의 소중한 소액 참여가 캠페인의 희망이 됩니다.";
                break;
            case LARGE_DONOR:
                if (campaign.getTargetAmount() >= 10000000) reason = "사회적 임팩트가 큰 대규모 프로젝트입니다. 당신의 큰 힘을 보여주세요!";
                break;
            case FREQUENT_DONOR:
                reason = "꾸준한 기부 천사님! 오늘 새롭게 등록된 따끈따끈한 캠페인입니다.";
                break;
            case NEWBIE:
                reason = "첫 기부의 설렘, 이 캠페인으로 시작해보는 건 어떨까요?";
                break;
        }

        return RecommendedCampaignResponse.builder()
                .campaignNo(campaign.getCampaignNo())
                .title(campaign.getTitle())
                .imagePath(campaign.getImagePath())
                .category(campaign.getCategory())
                .targetAmount(campaign.getTargetAmount())
                .currentAmount(campaign.getCurrentAmount())
                .achievementRate(rate)
                .recommendationReason(reason)
                .build();
    }
}

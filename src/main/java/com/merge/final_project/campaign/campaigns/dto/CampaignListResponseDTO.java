package com.merge.final_project.campaign.campaigns.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CampaignListResponseDTO {
    private Long campaignNo;

    // image ?åÏù¥Î∏?purpose='REPRESENTATIVE' ?∞Ïù¥??    private String imagePath;

    private String title;

    // Í∏∞Î? ?®Ï≤¥ ?¥Î¶Ñ (foundation ?åÏù¥Î∏îÍ≥º Ï°∞Ïù∏)
    private String foundationName;

    private Long targetAmount;
    private Long currentAmount;
    private String category;

    // D-Day Í≥ÑÏÇ∞ Î∞?ÎßàÍ∞ê ?ÑÎ∞ï???ïÎ†¨???¨Ïö©
    private LocalDateTime endAt;
}


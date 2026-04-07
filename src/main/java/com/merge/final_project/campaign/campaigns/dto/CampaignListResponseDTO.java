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

    // image 테이블 purpose='REPRESENTATIVE' 이미지
    private String imagePath;

    private String title;

    // 기부 단체 이름 (foundation 테이블과 조인)
    private String foundationName;

    private Long targetAmount;
    private Long currentAmount;
    private String category;

    // D-Day 계산 및 마감 임박순 정렬에 사용
    private LocalDateTime endAt;
}
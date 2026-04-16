package com.merge.final_project.donation.donations.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
//[바다] 기부 최근 5개 조회
public class RecentDonationFeedItemDTO {
    private String name;
    private Long amount;
    private LocalDateTime donatedAt;
    private String campaignTitle;
}

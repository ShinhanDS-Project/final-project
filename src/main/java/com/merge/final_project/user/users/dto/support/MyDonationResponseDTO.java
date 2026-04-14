package com.merge.final_project.user.users.dto.support;

import com.merge.final_project.campaign.campaigns.CampaignStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
public class MyDonationResponseDTO {
    //기부내역에서 조회해와야하는것들 캠페인 이름, 기부 금액,  기부 단계--
    //조회해와야할것들 : CampaignNo,donation_no, transaction_no, 마이크로트래킹, 증서

    // 1. 식별값 (JPA 순서: Long, Long, Long)
    private Long donationNo;
    private Long campaignNo;    // Donation 엔티티에 저장된 캠페인 번호
    private Long transactionNo; // 블록체인 트랜잭션 식별 번호

    // 2. 화면 표시 정보 (JPA 순서: String, String)
    private String campaignTitle;
    private String campaignImagePath;

    // 3. 기부 상세 정보 (JPA 순서: LocalDateTime, BigDecimal)
    private LocalDateTime donatedAt;   // 기부 일시
    private BigDecimal donationAmount; // 기부 금액 (정밀도를 위해 BigDecimal 권장)

    // 4. 상태 정보 (JPA 순서: Enum, String)
    // CampaignStatus: PENDING, RECRUITING, ENDED, ACTIVE, SETTLED, COMPLETED, CANCELLED
    private CampaignStatus campaignStatus;

    // tokenStatus: PENDING, PROCESSING, SUCCESS, FAILED
    private String tokenStatus;



}

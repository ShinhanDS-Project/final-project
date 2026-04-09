package com.merge.final_project.donation.donations.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class DonationSaveDTO {
    private Long paymentNo; // 방금 저장된 결제 테이블의 PK
    private Long userNo;// 기부한 유저의 고유 번호
    private Long campaignNo;// 기부 대상 캠페인 번호
    private BigDecimal amount;// 실제 기부된 금액 (donation_amount)
    private boolean isAnonymous;// 익명 기부 여부

    //토큰, 지갑은 여기에 추가? 잘 모르겠음
}

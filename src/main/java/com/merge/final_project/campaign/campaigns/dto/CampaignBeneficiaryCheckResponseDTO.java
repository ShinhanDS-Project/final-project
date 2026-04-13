package com.merge.final_project.campaign.campaigns.dto;

import lombok.Builder;
import lombok.Getter;

/* 캠페인 신청 시 입력한 엔트리 코드가 유효한 수혜자인지 확인한 결과를 담는 응답 객체 */
@Getter
@Builder
public class CampaignBeneficiaryCheckResponseDTO {
    private boolean valid; // 수혜자 여부 확인 결과 (true: 유효함, false: 유효하지 않음)
    private Long beneficiaryNo;
    private String entryCode;
    private String name; // 수혜자의 이름 (화면 표시용)
    private String beneficiaryType;
    private String message; // 검증 결과에 대한 안내 메시지 (예: "확인되었습니다" 또는 "코드가 일치하지 않습니다")
}
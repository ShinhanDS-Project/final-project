package com.merge.final_project.campaign.campaigns;

public enum CampaignStatus {
    // PENDING(관리자 승인 전), RECRUITING(관리자 승인 후 등록), ACTIVE(활동중), SETTLED(정산완료된 날), COMPLETED(보고서를 낸 날), CANCELLED(중간에 취소된 날)
    PENDING, RECRUITING, ACTIVE, ENDED, SETTLED, COMPLETED, CANCELLED
}
package com.merge.final_project.wallet.entity;

/**
 * 지갑 소유/용도 구분 타입.
 * owner_no 해석 방식은 wallet_type에 따라 달라진다.
 */
public enum WalletType {
    // 일반 기부자 사용자 지갑
    USER,
    // 기부단체 지갑
    FOUNDATION,
    // 수혜자 지갑
    BENEFICIARY,
    // 캠페인 전용 지갑
    CAMPAIGN,
    // 서버 운영용 HOT 지갑(가스/배정 송신)
    HOT,
    // 서버 운영용 COLD 지갑(보관용)
    COLD,
    // 레거시 데이터 호환용 서버 지갑 타입
    SERVER
}

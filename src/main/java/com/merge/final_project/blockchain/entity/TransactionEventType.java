package com.merge.final_project.blockchain.entity;

/**
 * 토큰/가스 이동의 도메인 이벤트 타입.
 * 일부 값은 레거시 데이터 호환을 위해 대소문자/명명 규칙이 혼재되어 있다.
 */
public enum TransactionEventType {
    // 일반 기부 전송
    DONATION,
    // 정산 수수료 전송
    SETTLEMENT_FEE,
    // 수혜자 정산 전송
    SETTLEMENT_BENEFICIARY,
    // 서버/소유자 -> 사용자 토큰 배정
    ALLOCATION,
    // 토큰 환급/환전
    REDEMPTION,
    // 결제 연동 토큰 충전 이벤트(레거시)
    PAYMENT_TOKEN_CHARGE,
    // 기부자 -> 캠페인 전송(레거시/신규 혼용)
    DONATION_TRANSFER,
    // 가스비 자동 충전
    POL_AUTO_TOPUP,
    // 과거 데이터 호환용 이벤트명(대소문자 그대로 유지)
    TokenAllocated
}

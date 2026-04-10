package com.merge.final_project.donation.payment;

public enum PaymentStatus {
    // 결제 생명주기를 관리
    READY , // 결제 생성 (인증전)
    IN_PROGRESS , // 결제 중
    DONE, //결제 성공 (돈이동)
    FAILED, // 결제 실패
    CANCELLED // 결제 취소
}

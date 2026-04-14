package com.merge.final_project.donation.payment;

public enum PaymentMethod {
    CARD("카드"),       // 일반 카드결제
    EASY_PAY("간편결제")    // 간편결제
    ,ETC("기타") ;//1. 부가기능 : 가상계좌 (시간이 있다면)

    private final String tossMethodName;

    PaymentMethod(String tossMethodName) {
        this.tossMethodName = tossMethodName;
    }

    // 토스가 주는 한글 값을 Enum의 한글 값과 비교하기 위한 Getter
    public String getTossMethodName() {
        return tossMethodName;
    }
}

package com.merge.final_project.blockchain.payment.event;

/**
 * 선우 작성:
 * 결제 승인 트랜잭션 커밋 이후 블록체인 후속 처리(토큰 충전/기부 전송)를 시작하기 위한 도메인 이벤트.
 * payload는 donationNo 하나만 유지해 결합도를 낮추고, 재처리 시에도 같은 식별자로 추적 가능하게 한다.
 */
public record PaymentConfirmedEvent(Long donationNo) {
}

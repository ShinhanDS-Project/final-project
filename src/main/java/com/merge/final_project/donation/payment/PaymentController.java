package com.merge.final_project.donation.payment;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Controller;

@Controller
@Transactional
public class PaymentController {
    // 1. 결제 생성요청
    // 프론트에서 서버에 보내는 값 : campaignId, amount -->isAnonymous?
    //예외조건 -> 캠페인 존재 여부 / 모금기간인지/ 최소 금액이상인지/회원 상태 / payment->pending으로 저장
    //프론트에게 내려줄 값:orderId, orderName,amount,customerName,customerEmail
    //성공하고 돌아오면 amount가 처음 요청한 금액과 같은지 검증



    // 2. 인증 후
        //예외조건 1. 캠페인 상태 확인
        //예외조건 2. 모금 기간내인지
        //예외조건 3. 유저 정보 확인
        //

    //3. 결제 승인 api 만들기
    //post /api/payments/confirm
    //프론트가 보낸ㄴ 값 :paymentKey,orderId,amount
    //예외조건 ->orderId로 pending payment 찾기, 이미 승인 된건 아닌지, db와 프론트가 보낸 amount가 같은지
    //캠페인이 아직 모금 기간 내인지
    // 최소 금액 정책 위반 아닌지 확인

    //4. 토스
}

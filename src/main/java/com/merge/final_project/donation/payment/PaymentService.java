package com.merge.final_project.donation.payment;

import com.merge.final_project.donation.payment.dto.PaymentConfirmRequest;
import com.merge.final_project.donation.payment.dto.PaymentConfirmResponse;
import com.merge.final_project.donation.payment.dto.PaymentReadyRequest;
import com.merge.final_project.donation.payment.dto.PaymentReadyResponse;
import org.springframework.http.ResponseEntity;

public interface PaymentService{
    //검증하고 client 호출하고 저장하는 용도-> 비지니스 로직 처리

    //1.결제하기 클릭하면 Payment 테이블에 상태값 ready로 저장
    public PaymentReadyResponse paymentReady(Long userNo,PaymentReadyRequest dto);

    //2. 결제 승인 : 사용자가 카드 인증 후 프론트에서 보내는 요청
    public PaymentConfirmResponse confirmPayment(Long userNo,PaymentConfirmRequest dto);

    //3. 결제 실패 :
    public void processPaymentFail(String orderId,String code, String message);
}

package com.merge.final_project.donation.payment;

import com.merge.final_project.donation.payment.dto.PaymentConfirmRequest;
import com.merge.final_project.donation.payment.dto.PaymentReadyRequest;
import com.merge.final_project.donation.payment.dto.PaymentReadyResponse;
import org.springframework.http.ResponseEntity;

public interface PaymentService{
    //검증하고 client 호출하고 저장하는 용도-> 비지니스 로직 처리
    //1. 시작 : 결제하기 누르자마자 payment생성
    public ResponseEntity<Payment> startPayment(PaymentReadyRequest dto);
    //1. 결제 1 검증하기
    PaymentReadyResponse readyPayment(Long userNo, PaymentReadyRequest request);

    //2. 결제2
    public void preparePayment(PaymentConfirmRequest dto);
    //2. 결제 실패
    public void processPaymentFail(String orderId, String code, String message);
}

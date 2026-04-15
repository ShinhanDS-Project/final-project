package com.merge.final_project.donation.payment;

import com.merge.final_project.donation.payment.dto.PaymentByUserResponse;
import com.merge.final_project.donation.payment.dto.PaymentConfirmRequest;
import com.merge.final_project.donation.payment.dto.PaymentConfirmResponse;
import com.merge.final_project.donation.payment.dto.PaymentReadyRequest;
import com.merge.final_project.donation.payment.dto.PaymentReadyResponse;

import java.util.List;

/**
 * 결제 서비스 진입점.
 *
 * 선우 작성 메모:
 * confirmPayment 내부에서 donation row 생성 후 PaymentConfirmedEvent를 발행하고,
 * 이후 블록체인 후속 처리는 blockchain.payment 오케스트레이터가 담당한다.
 */
public interface PaymentService {

    PaymentReadyResponse paymentReady(Long userNo, PaymentReadyRequest dto);

    PaymentConfirmResponse confirmPayment(Long userNo, PaymentConfirmRequest dto);

    List<PaymentByUserResponse> getPaymentHistoryByUser(Long userNo);
}

package com.merge.final_project.donation.payment;

import com.merge.final_project.donation.payment.dto.PaymentConfirmRequest;
import com.merge.final_project.donation.payment.dto.PaymentReadyRequest;
import com.merge.final_project.donation.payment.dto.PaymentReadyResponse;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@RestController
@Transactional
@RequestMapping("/api/payments")
public class PaymentController {
   @Autowired
    private PaymentService paymentService;
    @Autowired
    private PaymentRepository paymentRepository;
    @PostMapping("/ready")
    public ResponseEntity<PaymentReadyResponse> ready(
            @Valid @RequestBody PaymentReadyRequest request
    ) {
        Long loginUserNo = 1L; // 지금은 임시. 나중에 Security 붙이면 로그인 유저에서 꺼내기
        PaymentReadyResponse response = paymentService.readyPayment(loginUserNo, request);
        return ResponseEntity.ok(response);
    }
    @PostMapping("/confirm")
    public ResponseEntity<String> confirm(@Valid @RequestBody PaymentConfirmRequest dto) {
        paymentService.confirmPayment(dto);
        return ResponseEntity.ok("기부가 완료되었습니다. 감사합니다.");
    }


}

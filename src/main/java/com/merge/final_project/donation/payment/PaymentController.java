package com.merge.final_project.donation.payment;

import com.merge.final_project.donation.payment.dto.PaymentConfirmRequest;
import com.merge.final_project.donation.payment.dto.PaymentConfirmResponse;
import com.merge.final_project.donation.payment.dto.PaymentReadyRequest;
import com.merge.final_project.donation.payment.dto.PaymentReadyResponse;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {
   @Autowired
    private PaymentService paymentService;
    @Autowired
    private PaymentRepository paymentRepository;
    @PostMapping("/ready")
    public ResponseEntity<PaymentReadyResponse> ready(Authentication authentication,
                                                      @Valid @RequestBody PaymentReadyRequest request
    ) {
        //user에 대한 정보는 SecurityContext에서 꺼내와야한다.
        //jwt 필터에서 토큰 검증 끝낸뒤, Authentication안에 userno를 넣어두고 컨트롤러에서 꺼내기
        Long loginUserNo = (Long) authentication.getDetails();
        PaymentReadyResponse response = paymentService.paymentReady(loginUserNo, request);
        return ResponseEntity.ok(response);
    }
    @PostMapping("/confirm")
    public ResponseEntity<PaymentConfirmResponse> confirm(Authentication authentication, @Valid @RequestBody PaymentConfirmRequest dto) {
       //jwt 필터에서 저장한 값을 불러옴
        /*
        * getPrincipal() 하면 이메일 기반 User
          getDetails() 하면 Long pk
        * */
        Long loginUserNo = (Long) authentication.getDetails();
        PaymentConfirmResponse response=paymentService.confirmPayment(loginUserNo,dto);
        return ResponseEntity.ok(response);
    }


}

package com.merge.final_project.donation.payment;

import com.merge.final_project.donation.payment.dto.PaymentConfirmRequest;
import com.merge.final_project.donation.payment.dto.PaymentConfirmResponse;
import com.merge.final_project.donation.payment.dto.PaymentReadyRequest;
import com.merge.final_project.donation.payment.dto.PaymentReadyResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@io.swagger.v3.oas.annotations.tags.Tag(name = "결제", description = "기부 결제(카카오페이 등) 준비·확인 API")
@RestController
@RequestMapping("/api/payments")
public class PaymentController {
   @Autowired
    private PaymentService paymentService;
    @Autowired
    private PaymentRepository paymentRepository;
    @Operation(summary = "결제 준비", description = "카카오페이 등 결제 수단으로 기부 결제를 준비합니다. 결제 URL을 반환합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "준비 성공 — 결제 URL 반환"),
            @ApiResponse(responseCode = "400", description = "요청 값 유효성 오류"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 요청")
    })
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
    @Operation(summary = "결제 확인", description = "결제 완료 후 결제 결과를 확인하고 기부를 확정합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "확인 성공 — 기부 확정"),
            @ApiResponse(responseCode = "400", description = "요청 값 유효성 오류"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 요청")
    })
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

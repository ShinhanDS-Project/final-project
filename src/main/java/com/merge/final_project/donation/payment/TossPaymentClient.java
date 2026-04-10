package com.merge.final_project.donation.payment;

import com.merge.final_project.donation.payment.dto.PaymentBody;
import com.merge.final_project.donation.payment.dto.PaymentConfirmRequest;
import com.merge.final_project.donation.payment.dto.TossResponse;
import com.merge.final_project.global.exceptions.BusinessException;
import com.merge.final_project.global.exceptions.ErrorCode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.Base64;

@Component
public class TossPaymentClient {

    @Value("${toss.secret.key}")
    private String secretKey;
    //restTemplate 코드 -> 토스 외부 api 호출
    private final RestTemplate restTemplate = new RestTemplate();

    public PaymentBody confirmPayment(PaymentConfirmRequest requestDTO){

        String url="https://api.tosspayments.com/v1/payments/confirm";
        //1. 헤더 설정 (시크릿키 +":"->base64인코딩)
        HttpHeaders headers = new HttpHeaders();
        String encodedKey= Base64.getEncoder().encodeToString((secretKey+":").getBytes());
        headers.set("Authorization", "Basic "+encodedKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<PaymentConfirmRequest> entity = new HttpEntity<>(requestDTO,headers);

        //2. 토스 서버에 승인 요청 전송
        //응답을 tossResponse<PayMentBody>형태로 받음
        // 토스가 응답을 한번더 감싸서 와서 tossResponse<T>로 받음
       try {
           ResponseEntity<PaymentBody> response = restTemplate.exchange(
                   url,
                   HttpMethod.POST,
                   entity,
                   PaymentBody.class
           );
           return response.getBody();
       }
       catch(HttpClientErrorException e){
           //400번대 에러
           //토스가 보낸 에러를 확인 후 던wla
           throw new BusinessException(ErrorCode.PAYMENT_CONFIRM_FAILED);
       }catch (Exception e){
           //네트워크등 기타 에러
           throw new BusinessException(ErrorCode.PAYMENT_CONFIRM_FAILED);
       }
        // 3. 알맹이(entityBody)만 쏙 빼서 반환

    }
    //추가기능
    //에러 핸들링 :confirm Payment 메서드에 감싸서 로그 남기기
    //로그: log.error써서 로그 잡기
}

package com.merge.final_project.donation.payment;

import com.merge.final_project.donation.payment.dto.PaymentBody;
import com.merge.final_project.donation.payment.dto.PaymentConfirmRequest;
import com.merge.final_project.global.exceptions.BusinessException;
import com.merge.final_project.global.exceptions.ErrorCode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Component
public class TossPaymentClient {

    @Value("${TOSS_SECRET_KEY:}")
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

        Map<String, Object> tossRequestBody = new HashMap<>();
        tossRequestBody.put("paymentKey", requestDTO.getPaymentKey());
        tossRequestBody.put("orderId", requestDTO.getOrderId());
        tossRequestBody.put("amount", requestDTO.getAmount());

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(tossRequestBody,headers);
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
           //토스가 보낸 에러를 확인 후 던짐
//           System.err.println("##### 토스 API 에러 발생 #####");
//           System.err.println("상태 코드: " + e.getStatusCode());
//           System.err.println("에러 본문: " + e.getResponseBodyAsString()); // 이게 핵심입니다!

           throw new BusinessException(ErrorCode.PAYMENT_CONFIRM_FAILED);
       }catch (Exception e){
           //네트워크등 기타 에러
//           System.err.println("##### 기타 네트워크 에러 #####");
//           e.printStackTrace(); // 어디서 터졌는지 스택트레이스 전체 출력
           throw new BusinessException(ErrorCode.PAYMENT_CONFIRM_FAILED);
       }
        // 3. 알맹이(entityBody)만 쏙 빼서 반환

    }
    // 결제는 됐는데, 만약의 예외상황으로 기부가 불가능해진 경우를 고려.
    public void cancelPayment(String paymentKey, String cancelReason) {
        String url = "https://api.tosspayments.com/v1/payments/" + paymentKey + "/cancel";

        HttpHeaders headers = new HttpHeaders();
        String encodedKey = Base64.getEncoder().encodeToString((secretKey + ":").getBytes());
        headers.set("Authorization", "Basic " + encodedKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = new HashMap<>();
        body.put("cancelReason", cancelReason);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        try {
            restTemplate.postForEntity(url, entity, Object.class);
        } catch (Exception e) {
            // 취소 요청 자체가 실패한 경우
            //System.err.println("!!! 결제 취소 요청 실패 !!! paymentKey: " + paymentKey);
          //  e.printStackTrace();
        }
    }
}

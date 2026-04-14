package com.merge.final_project.donation.payment;

import com.merge.final_project.campaign.campaigns.CampaignStatus;
import com.merge.final_project.blockchain.payment.event.PaymentConfirmedEvent;
import com.merge.final_project.campaign.campaigns.entity.Campaign;
import com.merge.final_project.campaign.campaigns.repository.CampaignRepository;
import com.merge.final_project.donation.donations.Donation;
import com.merge.final_project.donation.donations.DonationRepository;
import com.merge.final_project.donation.donations.DonationTokenStatus;
import com.merge.final_project.donation.payment.dto.*;
import com.merge.final_project.global.exceptions.BusinessException;
import com.merge.final_project.global.exceptions.ErrorCode;
import com.merge.final_project.org.AccountStatus;
import com.merge.final_project.org.Foundation;
import com.merge.final_project.org.FoundationRepository;
import com.merge.final_project.user.users.User;
import com.merge.final_project.user.users.UserRepository;
import com.merge.final_project.wallet.repository.WalletRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Transactional
@Service
/**
 * 결제 도메인 서비스 구현체.
 *
 * 선우 작성 메모:
 * 이 클래스는 결제 확정/검증과 donation 생성까지만 책임지고,
 * 블록체인 전송(토큰 충전/기부)은 이벤트 발행으로 분리한다.
 */
public class PaymentServiceImpl implements PaymentService {

    @Autowired
    private PaymentRepository paymentRepository;
    @Autowired
    private DonationRepository donationRepository;
    @Autowired
    private TossPaymentClient tossPaymentClient;
    @Autowired
    private CampaignRepository campaignRepository;
    @Autowired
    FoundationRepository foundationRepository;
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WalletRepository walletRepository;
    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Override
    public PaymentReadyResponse paymentReady(Long userNo, PaymentReadyRequest dto) {
        //2.캠페인
        //1) 캠페인이 존재하는지
        Campaign campaign = campaignRepository.findById(dto.getCampaignNo())
                .orElseThrow(() -> new BusinessException(ErrorCode.CAMPAIGN_NOT_FOUND));

        // 2)캠페인의 상태 확인
        if (!CampaignStatus.ACTIVE.equals(campaign.getCampaignStatus())) {
            throw new BusinessException(ErrorCode.CAMPAIGN_NOT_ACTIVE);
        }
        // 3) 캠페인 모금 기간 외이면
        if (LocalDateTime.now().isBefore(campaign.getStartAt()) || LocalDateTime.now().isAfter(campaign.getEndAt())) {
            throw new BusinessException(ErrorCode.CAMPAIGN_NOT_ACTIVE);
        }

        //4. 이상한 금액이 나오는 경우  (음수, 0원 막기 )
         if (dto.getAmount() == null || dto.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
             throw new BusinessException(ErrorCode.INVALID_DONATION_AMOUNT);
         }

        String orderId = "DONATION-" +  UUID.randomUUID() ;
        //2. 결제 대기 (READY) 데이터 생성
        Payment payment = Payment.builder()
                .userNo(userNo)
                .campaignNo(dto.getCampaignNo())
                .orderKey(orderId)
                .amount(dto.getAmount())
                .method(dto.getMethod())
                .paymentStatus(PaymentStatus.READY)
                .privateKeyNo(1L)// -> 월렛팀 이거 해결해주세요.. 왜필요한지 모르겠어요
                .isAnonymous(dto.getIsAnonymous())

                .build();
        paymentRepository.save(payment);
        //3.프론트에 전송할 정보
        return PaymentReadyResponse.builder()
                .paymentNo(payment.getPaymentNo())
                .orderId(orderId)
                .amount(payment.getAmount())
                .orderName(campaign.getTitle())
                .build();
    }

    @Override
    public PaymentConfirmResponse confirmPayment(Long userNo, PaymentConfirmRequest dto) {
        //1. 유저
        // 1) 유저가 존재하는지 확인
            User user = userRepository.findById(userNo)
                    .orElseThrow(() ->
                            new BusinessException(ErrorCode.USER_NOT_FOUND)
                    );
        //2) 지갑여부 체크-> 지갑 처리가 제대로 된다는 가정하에 주석 풀기
//        if (user.getWallet() == null) {
//            throw new BusinessException(ErrorCode.USER_WALLET_NOT_FOUND);
//        }

        //2. 결제건

            // 1) 존재하는 결제건인지 확인
            Payment payment = paymentRepository.findByOrderKeyAndUserNo(dto.getOrderId(),user.getUserNo())
                    .orElseThrow(() -> new BusinessException(ErrorCode.PAYMENT_NOT_FOUND));

            // 2) 결제 상태가 ready인 건만 승인
            if (payment.getPaymentStatus() != PaymentStatus.READY) {
                throw new BusinessException(ErrorCode.PAYMENT_INVALID_STATUS);
            }

            // 3) 금액이 바뀌지 않았는지 확인
            if (dto.getAmount().compareTo(payment.getAmount()) != 0) {
                throw new BusinessException(ErrorCode.PAYMENT_AMOUNT_MISMATCH);
            }


            // 4) 결제 메서드가 동일한지
            if (!dto.getMethod().equals(payment.getMethod())) {
                throw new BusinessException(ErrorCode.PAYMENT_METHOD_MISMATCH);
            }



        //3. 캠페인
            // 1) 캠페인 존재여부
            Campaign campaign = campaignRepository.findById(payment.getCampaignNo())
                    .orElseThrow(() -> new BusinessException(ErrorCode.CAMPAIGN_NOT_FOUND));

            // 2) 캠페인 상태와 기간을 체크
            if (!CampaignStatus.ACTIVE.equals(campaign.getCampaignStatus())) {
                throw new BusinessException(ErrorCode.CAMPAIGN_NOT_ACTIVE);
            }

            if (LocalDateTime.now().isBefore(campaign.getStartAt()) || LocalDateTime.now().isAfter(campaign.getEndAt())) {
                throw new BusinessException(ErrorCode.CAMPAIGN_NOT_ACTIVE);
            }

//            //4. 캠페인 지갑
//            // 1) 월렛 주소가 없는 경우
//            if (campaign.getWalletNo() == null) {
//                throw new BusinessException(ErrorCode.CAMPAIGN_WALLET_NOT_FOUND);
//            }
//            // 2) 지갑이 지금은 존재하지 않는 경우
//            Wallet wallet = walletRepository.findById(campaign.getWalletNo())
//                    .orElseThrow(() -> new BusinessException(ErrorCode.CAMPAIGN_WALLET_NOT_FOUND));


            // 5.기부단체
            // 1) 기부단체가 존재하는지
            long foundationId = campaign.getFoundationNo();
            Foundation foundation = foundationRepository.findById(foundationId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.FOUNDATION_NOT_FOUND));

            //2) 기부단체 상태 체크
            if (AccountStatus.INACTIVE.equals(foundation.getAccountStatus())) {
                throw new BusinessException(ErrorCode.FOUNDATION_INACTIVE);
            }
            //3)
            if (donationRepository.existsByPaymentNo(payment.getPaymentNo())) {
                throw new BusinessException(ErrorCode.DUPLICATE_DONATION);
            }

        //4. 토스 호출=> 결제 완료
        PaymentBody paymentBody;
        try {
            paymentBody = tossPaymentClient.confirmPayment(dto);
        } catch (Exception e) {
            // 승인 요청 자체가 실패한 건 토스 쪽 결제가 안 된 거니 취소할 필요 없음
            throw new BusinessException(ErrorCode.PAYMENT_CONFIRM_FAILED);
        }

        //예외 발생 조건 확인-> 결제 완료된 시점에서 확인하기
        try {
            // 1)토스 승인 api 호출


            // 2) 토스 응답 기준 재검증
            //3-1. 금액이 맞지 않는경우
            if (paymentBody.getTotalAmount().compareTo(payment.getAmount()) != 0) {
                throw new BusinessException(ErrorCode.PAYMENT_AMOUNT_MISMATCH);
            }
            //3-2. payment 키가 존재하면 -> 이미 결제된 건
            if (paymentRepository.existsByPaymentKey(paymentBody.getPaymentKey())) {
                throw new BusinessException(ErrorCode.DUPLICATE_PAYMENT_KEY);
            }

            // 3) 결제 수단 위변조 최종 확인 (이 부분이 현재 빠져있습니다!)
            String expectedMethodName = payment.getMethod().getTossMethodName(); // "카드"
            if (!expectedMethodName.equals(paymentBody.getMethod())) {
                throw new BusinessException(ErrorCode.PAYMENT_METHOD_MISMATCH);
            }


            // 4) 기존 Payment 엔티티 업데이트
            payment.setPaymentStatus(PaymentStatus.DONE);
            payment.setPaymentKey(paymentBody.getPaymentKey());
            payment.setPaidAt(paymentBody.getApprovedAt().toLocalDateTime());


            //5. 기부 생성
            // 선우 작성:
            // 결제 성공 직후 donation row를 PENDING으로 만들고,
            // 실제 온체인 처리(충전/기부 전송)는 AFTER_COMMIT 이벤트로 비동기 위임한다.
            Donation donation = Donation.builder()
                    .paymentNo(payment.getPaymentNo())
                    .userNo(userNo)
                    .campaignNo(payment.getCampaignNo())
                    .donationAmount(paymentBody.getTotalAmount())
                    .donatedAt(LocalDateTime.now())
                    .isAnonymous(payment.getIsAnonymous())
                    .tokenStatus(DonationTokenStatus.PENDING.name())
                    // .donorWalletNo(user.getWallet().getWalletNo())
                    // .campaignWalletNo(campaign.getWalletNo())
                    //.keyNo()
                    .build();

            donationRepository.save(donation);
            //6. 캠페인 현재 모금액 합산
            campaign.addCurrentAmount(paymentBody.getTotalAmount());
            // 선우 작성:
            // 이벤트는 트랜잭션 커밋 이후에 수신되므로
            // 결제/기부 DB 상태가 확정된 데이터만 블록체인 후속 처리로 전달된다.
            eventPublisher.publishEvent(new PaymentConfirmedEvent(donation.getDonationNo()));

            return PaymentConfirmResponse.builder()
                    .paymentNo(payment.getPaymentNo())
                    .donationNo(donation.getDonationNo())
                    .orderId(payment.getOrderKey())
                    .paymentKey(payment.getPaymentKey())
                    .amount(payment.getAmount())
                    .status("SUCCESS")
                    .message("기부가 완료되었습니다. 감사합니다!")
                    .build();

        }catch(BusinessException be){
            // 우리가 의도해서 터뜨린 에러(위변조 등)는 에러 코드를 유지한 채로 환불
            tossPaymentClient.cancelPayment(paymentBody.getPaymentKey(), "검증 실패로 인한 자동 취소");
            payment.setPaymentStatus(PaymentStatus.FAILED);
            throw be; // 원래 에러(예: PAYMENT_AMOUNT_MISMATCH)를 프론트로 그대로 던짐

        }

        catch(Exception e){
            tossPaymentClient.cancelPayment(paymentBody.getPaymentKey(), "서버 내부 오류로 인한 자동 취소");

            // 결제 상태를 실패로 변경
            payment.setPaymentStatus(PaymentStatus.FAILED);

            throw new BusinessException(ErrorCode.DONATION_CREATE_FAILED);
        }




    }

    @Override
    public List<PaymentByUserResponse> getPaymentHistoryByUser(Long userNo) {
        List<PaymentByUserResponse> listPayment= paymentRepository.findByUserNo(userNo);
        return listPayment;
    }

    @Transactional
    public void processPaymentFail(String orderId, String code, String message) {
        paymentRepository.findByOrderKey(orderId).ifPresent(payment -> {
            // 이미 성공한 결제는 실패로 덮어쓰지 않음
            if (payment.getPaymentStatus() != PaymentStatus.READY) {
                return;
            }

            payment.setPaymentStatus(PaymentStatus.FAILED);
            // TODO: failCode, failReason 필드 추가 후 저장
            // payment.setFailCode(code);
            // payment.setFailReason(message);
        });
    }
}

package com.merge.final_project.donation.payment;

import com.merge.final_project.campaign.campaigns.CampaignStatus;
import com.merge.final_project.campaign.campaigns.entity.Campaign;
import com.merge.final_project.campaign.campaigns.repository.CampaignRepository;
import com.merge.final_project.donation.donations.Donation;
import com.merge.final_project.donation.donations.DonationRepository;
import com.merge.final_project.donation.payment.dto.*;
import com.merge.final_project.global.exceptions.BusinessException;
import com.merge.final_project.global.exceptions.ErrorCode;
import com.merge.final_project.org.AccountStatus;
import com.merge.final_project.org.Foundation;
import com.merge.final_project.org.FoundationRepository;
import com.merge.final_project.user.users.User;
import com.merge.final_project.user.users.UserRepository;
import com.merge.final_project.wallet.entity.Wallet;
import com.merge.final_project.wallet.repository.WalletRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
@Transactional
@Service
public class PaymentServiceImpl implements PaymentService{

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

    @Override
    public PaymentReadyResponse paymentReady(Long userNo, PaymentReadyRequest dto) {
        //2.캠페인
        //1) 캠페인이 존재하는지
        Campaign campaign=campaignRepository.findById(dto.getCampaignNo())
                .orElseThrow(()->new BusinessException(ErrorCode.CAMPAIGN_NOT_FOUND));

        // 2)캠페인이 모금기간인지
        if(!CampaignStatus.ACTIVE.equals(campaign.getCampaignStatus())){
            throw new BusinessException(ErrorCode.CAMPAIGN_NOT_ACTIVE);
        }
        String orderId="DONATION-"+System.currentTimeMillis()+"-"+userNo;
        //2. 결제 대기 (READY) 데이터 생성
        Payment payment= Payment.builder()
                .userNo(userNo)
                .campaignNo(dto.getCampaignNo())
                .orderKey(orderId)
                .amount(dto.getAmount())
                .paymentMethod(dto.getPaymentMethod())
                .paymentStatus(PaymentStatus.READY)
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
        User user= userRepository.findById(userNo)
                .orElseThrow(()->  new BusinessException(ErrorCode.USER_NOT_FOUND));

        //2. 결제건
        // 1) 존재하는 결제건인지 확인
        Payment payment= paymentRepository.findByOrderKey(dto.getOrderId())
                .orElseThrow(()->new BusinessException(ErrorCode.PAYMENT_NOT_FOUND));

        // 2) 금액이 바뀌지 않았는지 확인
        if(dto.getAmount().compareTo(payment.getAmount())!=0 ){
            throw new BusinessException(ErrorCode.PAYMENT_AMOUNT_MISMATCH);
        }
        // 3) 결제 메서드가 동일한지
        if(!dto.getMethod().equals(payment.getPaymentMethod())){
            throw new BusinessException(ErrorCode.PAYMENT_METHOD_MISMATCH);
        }

        //3. 캠페인
        Campaign campaign=campaignRepository.findById( payment.getCampaignNo())
                .orElseThrow(()-> new BusinessException(ErrorCode.CAMPAIGN_NOT_FOUND));

        //4. 캠페인 지갑
        // 1) 월렛 주소가 없는 경우
        if(campaign.getWalletNo()==null){
            throw new BusinessException(ErrorCode.CAMPAIGN_WALLET_NOT_FOUND);
        }
        // 2) 지갑이 지금은 존재하지 않는 경우
        Wallet wallet = walletRepository.findById(campaign.getWalletNo())
                .orElseThrow(()-> new BusinessException(ErrorCode.CAMPAIGN_WALLET_NOT_FOUND));


        // 3.기부단체
        // 1) 기부단체가 존재하는지
        long foundationId=campaign.getFoundationNo();
        Foundation foundation=foundationRepository.findById(foundationId)
                .orElseThrow(()->  new BusinessException(ErrorCode.FOUNDATION_NOT_FOUND));

        //2) 기부단체 상태 체크
        if(AccountStatus.INACTIVE.equals(foundation.getAccountStatus())){
            throw new BusinessException(ErrorCode.FOUNDATION_INACTIVE);
        }
        //4. 토스 호출
        // 1)토스 승인 api 호출
        PaymentBody paymentBody = tossPaymentClient.confirmPayment(dto);

        // 1. 기존 Payment 엔티티 업데이트
        payment.setPaymentStatus(PaymentStatus.DONE);
        payment.setPaymentKey(paymentBody.getPaymentKey());
        payment.setPaidAt(paymentBody.getApprovedAt().toLocalDateTime());


        //5. 기부 생성
        Donation donation = Donation.builder()
                .paymentNo(payment.getPaymentNo())
                .userNo(userNo)
                .campaignNo(payment.getCampaignNo())
                .donationAmount(paymentBody.getTotalAmount())
                .donatedAt(LocalDateTime.now())
                .isAnonymous(payment.getIsAnonymous())
                .donorWalletNo(user.getWallet().getWalletNo())
                .campaignWalletNo(campaign.getWalletNo())
                .build();

        donationRepository.save(donation);
        //6. 캠페인 현재 모금액 합산
        campaign.addCurrentAmount(paymentBody.getTotalAmount());

        return PaymentConfirmResponse.builder()
                .paymentNo(payment.getPaymentNo())
                .donationNo(donation.getDonationNo())
                .orderId(payment.getOrderKey())
                .paymentKey(dto.getPaymentKey())
                .amount(payment.getAmount())
                .status("SUCCESS")
                .message("기부가 완료되었습니다. 감사합니다!")
                .build();


    }

    @Transactional
    public void processPaymentFail(String orderId, String code, String message) {
        Payment payment = paymentRepository.findByOrderKey(orderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PAYMENT_NOT_FOUND));

        // 상태를 FAILED로 변경하고 실패 사유를 기록 (엔티티에 메서드 필요)
        payment.setPaymentStatus(PaymentStatus.FAILED);
    }

}

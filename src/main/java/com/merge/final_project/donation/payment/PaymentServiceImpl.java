package com.merge.final_project.donation.payment;

import com.merge.final_project.campaign.campaigns.CampaignStatus;
import com.merge.final_project.campaign.campaigns.entity.Campaign;
import com.merge.final_project.campaign.campaigns.repository.CampaignRepository;
import com.merge.final_project.donation.donations.Donation;
import com.merge.final_project.donation.donations.DonationRepository;
import com.merge.final_project.donation.payment.dto.PaymentBody;
import com.merge.final_project.donation.payment.dto.PaymentConfirmRequest;
import com.merge.final_project.donation.payment.dto.PaymentReadyRequest;
import com.merge.final_project.donation.payment.dto.PaymentReadyResponse;
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

    //1) 준비단계 -> 검증 (비지니스 로직 )


    @Override
    public void preparePayment(PaymentConfirmRequest dto, Long userNo) {

        //1. 유저
        // 1) 유저가 존재하는지 확인
        User user= userRepository.findById(userNo)
                .orElseThrow(()-> new RuntimeException("에러가 발생하였습니다."));

        //1. 결제건
        // 1) 존재하는 결제건인지 확인
        Payment payment= paymentRepository.findByOrderKey(dto.getOrderId())
                .orElseThrow(()->new RuntimeException("찾을 수 없는 결제 내역입니다."));

        // 2) 금액이 바뀌지 않았는지 확인
        if(dto.getAmount().compareTo(payment.getAmount())!=0 ){
            //금액이 달라진 오류 코드 발생 -> " 오류가 발생했습니다 "
        }

        //2.캠페인
        //1) 캠페인이 존재하는지
        Campaign campaign=campaignRepository.findById( payment.getCampaignNo())
                .orElseThrow(()->new RuntimeException("찾을 수 없는 캠페인입니다."));

        // 2)캠페인이 모금기간인지
        if(!CampaignStatus.ACTIVE.equals(campaign.getCampaignStatus())){
            //에러코드 -> 현재 모금기간이 아닙니다.
        }
        //3. 캠페인 지갑 유효한지 확인
        // 1) 월렛 주소가 없는 경우
        if(campaign.getWalletNo()==null){
            new RuntimeException("오류가 발생하였습니다.");
        }
        // 2) 지갑이 지금은 존재하지 않는 경우
        Wallet wallet = walletRepository.findById(campaign.getWalletNo())
                .orElseThrow(()->new RuntimeException("오류가 발생하였습니다."));


        // 3.기부단체
        // 1) 기부단체가 존재하는지
        long foundationId=campaign.getFoundationNo();
        Foundation foundation=foundationRepository.findById(foundationId)
                .orElseThrow(()-> new RuntimeException("찾을 수 없는 기부단체입니다."));

        //2) 기부단체 상태 체크
        if(AccountStatus.INACTIVE.equals(foundation.getAccountStatus())){
            //에러코드 체크 -- > 존재하지 않는경우
        }

        //토스 승인 api 호출
        PaymentBody paymentBody = tossPaymentClient.confirmPayment(dto);

        // 성공시엔 상태 변경 후 기부내역 생성
        // 1. 기존 Payment 엔티티 업데이트
        payment.setPaymentStatus(PaymentStatus.COMPLETED);

        // JPA 영속성 컨텍스트 덕분에 save를 다시 안 해도 트랜잭션 종료 시 업데이트됨.


        // 2. Donation(기부 내역) 생성
        //1.유저 월렛 찾아내기

        Donation donation = Donation.builder()
                .paymentNo(payment.getPaymentNo())
                .userNo(userNo)
                .campaignNo(payment.getCampaignNo())
                .donationAmount(paymentBody.getTotalAmount())
                .donatedAt(LocalDateTime.now())           // 기부일시
                .isAnonymous(payment.getIsAnonymous())
                .donorWalletNo(user.getWallet().getWalletNo())
                .campaignWalletNo(campaign.getWalletNo())
                .build();
        donationRepository.save(donation);

        // 3. 캠페인 현재 모금액 합산
        campaign.addCurrentAmount(paymentBody.getTotalAmount());
    }

    //
    @Override
    public ResponseEntity<Payment> startPayment(PaymentReadyRequest dto, Long userNo) {
      //
        Payment.builder()
              .paymentMethod(dto.getPaymentMethod())
              .campaignNo(dto.getCampaignNo())
              .amount(dto.getAmount())
              .paymentStatus(PaymentStatus.READY)
                .userNo(userNo)
                .isAnonymous(dto.getIsAnonymous())
                .
                .
              //.privatekeyNo()
    }

    @Override
    public PaymentReadyResponse readyPayment(Long userNo, PaymentReadyRequest request) {
        return null;
    }

    @Override
    public void preparePayment(PaymentConfirmRequest dto) {

    }

    @Transactional
    public void processPaymentFail(String orderId, String code, String message) {
        Payment payment = paymentRepository.findByOrderKey(orderId)
                .orElseThrow(() -> new RuntimeException("결제 내역 없음"));

        // 상태를 FAILED로 변경하고 실패 사유를 기록 (엔티티에 메서드 필요)
        payment.setPaymentStatus(PaymentStatus.FAILED);
    }

}

package com.merge.final_project.payment;
import com.merge.final_project.campaign.campaigns.CampaignStatus;
import com.merge.final_project.campaign.campaigns.entity.Campaign;
import com.merge.final_project.campaign.campaigns.repository.CampaignRepository;
import com.merge.final_project.donation.donations.Donation;
import com.merge.final_project.donation.donations.DonationRepository;
import com.merge.final_project.donation.payment.*;
import com.merge.final_project.donation.payment.dto.PaymentBody;
import com.merge.final_project.donation.payment.dto.PaymentConfirmRequest;
import com.merge.final_project.donation.payment.dto.PaymentConfirmResponse;
import com.merge.final_project.donation.payment.dto.PaymentReadyRequest;
import com.merge.final_project.global.exceptions.BusinessException;
import com.merge.final_project.global.exceptions.ErrorCode;
import com.merge.final_project.org.AccountStatus;
import com.merge.final_project.org.Foundation;
import com.merge.final_project.org.FoundationRepository;
import com.merge.final_project.user.users.User;
import com.merge.final_project.user.users.UserRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
@ExtendWith(MockitoExtension.class)
@Transactional
public class PaymentDonationTest {

    @InjectMocks
    private PaymentServiceImpl paymentService;

    @Mock private PaymentRepository paymentRepository;
    @Mock private DonationRepository donationRepository;
    @Mock private TossPaymentClient tossPaymentClient;
    @Mock private CampaignRepository campaignRepository;
    @Mock private FoundationRepository foundationRepository;
    @Mock private UserRepository userRepository;

    private User user;
    private Campaign campaign;
    private Payment payment;
    private Foundation foundation;

    @BeforeEach
    void setUp() {
        user = User.builder().userNo(1L).build();
        campaign = Campaign.builder()
                .campaignNo(216L)
                .campaignStatus(CampaignStatus.ACTIVE)
                .startAt(LocalDateTime.now().minusDays(1))
                .endAt(LocalDateTime.now().plusDays(1))
                .foundationNo(50L)
                .currentAmount(BigDecimal.ZERO)
                .build();
        payment = Payment.builder()
                .paymentNo(10L)
                .userNo(1L)
                .amount(new BigDecimal("1000"))
                .method(PaymentMethod.CARD)
                .paymentStatus(PaymentStatus.READY)
                .build();
        foundation = Foundation.builder()
                .foundationNo(50L)
                .accountStatus(AccountStatus.ACTIVE)
                .build();
    }

    @Nested
    @DisplayName("결제 준비(paymentReady) 예외 테스트")
    class ReadyExceptions {
        @Test
        @DisplayName("실패: 캠페인이 존재하지 않으면 예외가 발생한다")
        void ready_CampaignNotFound() {
            given(campaignRepository.findById(anyLong())).willReturn(Optional.empty());
            assertThrows(BusinessException.class, () -> paymentService.paymentReady(1L, new PaymentReadyRequest()));
        }

        @Test
        @DisplayName("실패: 기부 금액이 0원 이하이면 예외가 발생한다")
        void ready_InvalidAmount() {
            PaymentReadyRequest request = mock(PaymentReadyRequest.class);
            given(request.getAmount()).willReturn(BigDecimal.ZERO);
            given(campaignRepository.findById(anyLong())).willReturn(Optional.of(campaign));

            BusinessException ex = assertThrows(BusinessException.class, () -> paymentService.paymentReady(1L, request));
            assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.INVALID_DONATION_AMOUNT);
        }
    }

    @Nested
    @DisplayName("결제 승인(confirmPayment) 예외 테스트")
    class ConfirmExceptions {

        @Test
        @DisplayName("실패: 결제 요청 금액과 DB 저장 금액이 다르면 승인되지 않는다")
        void confirm_AmountMismatch() {
            PaymentConfirmRequest dto = mock(PaymentConfirmRequest.class);
            given(dto.getAmount()).willReturn(new BigDecimal("5000")); // 요청은 5000원
            given(dto.getOrderId()).willReturn("order-1");

            given(userRepository.findById(anyLong())).willReturn(Optional.of(user));
            given(paymentRepository.findByOrderKeyAndUserNo(anyString(), anyLong())).willReturn(Optional.of(payment)); // DB는 1000원

            BusinessException ex = assertThrows(BusinessException.class, () -> paymentService.confirmPayment(1L, dto));
            assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.PAYMENT_AMOUNT_MISMATCH);
        }

        @Test
        @DisplayName("실패: 이미 처리된 기부(중복 결제)인 경우 예외가 발생한다")
        void confirm_DuplicateDonation() {
            PaymentConfirmRequest dto = mock(PaymentConfirmRequest.class);
            given(dto.getAmount()).willReturn(new BigDecimal("1000"));
            given(dto.getMethod()).willReturn(PaymentMethod.CARD);

            given(userRepository.findById(anyLong())).willReturn(Optional.of(user));
            given(paymentRepository.findByOrderKeyAndUserNo(any(), any())).willReturn(Optional.of(payment));
            given(campaignRepository.findById(any())).willReturn(Optional.of(campaign));
            given(foundationRepository.findById(anyLong())).willReturn(Optional.of(foundation));

            // 이미 기부가 존재한다고 가정
            given(donationRepository.existsByPaymentNo(anyLong())).willReturn(true);

            BusinessException ex = assertThrows(BusinessException.class, () -> paymentService.confirmPayment(1L, dto));
            assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.DUPLICATE_DONATION);
        }

        @Test
        @DisplayName("🚨 핵심: 우리 서버 에러 시 토스 결제가 자동으로 취소되어야 한다")
        void confirm_InternalError_TriggersCancel() {
            // Given
            PaymentConfirmRequest dto = mock(PaymentConfirmRequest.class);
            given(dto.getAmount()).willReturn(new BigDecimal("1000"));
            given(dto.getMethod()).willReturn(PaymentMethod.CARD);

            PaymentBody tossResponse = mock(PaymentBody.class);
            given(tossResponse.getPaymentKey()).willReturn("fake-key");
            given(tossResponse.getTotalAmount()).willReturn(new BigDecimal("1000"));
            given(tossResponse.getApprovedAt()).willReturn(OffsetDateTime.now());

            given(userRepository.findById(anyLong())).willReturn(Optional.of(user));
            given(paymentRepository.findByOrderKeyAndUserNo(any(), any())).willReturn(Optional.of(payment));
            given(campaignRepository.findById(any())).willReturn(Optional.of(campaign));
            given(foundationRepository.findById(anyLong())).willReturn(Optional.of(foundation));
            given(tossPaymentClient.confirmPayment(any())).willReturn(tossResponse);

            // 기부 저장 시 런타임 에러 강제 발생 (DB 연결 끊김 등 가정)
            doThrow(new RuntimeException("DB Connection Error")).when(donationRepository).save(any());

            // When & Then
            assertThrows(BusinessException.class, () -> paymentService.confirmPayment(1L, dto));

            // ⭐ 중요: 에러가 났을 때 cancelPayment가 호출되었는지 검증!
            verify(tossPaymentClient, times(1)).cancelPayment(eq("fake-key"), anyString());
            assertThat(payment.getPaymentStatus()).isEqualTo(PaymentStatus.FAILED);
        }
    }
}

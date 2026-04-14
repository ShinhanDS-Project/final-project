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
import com.merge.final_project.donation.payment.dto.PaymentReadyResponse;
import com.merge.final_project.global.exceptions.BusinessException;
import com.merge.final_project.global.exceptions.ErrorCode;
import com.merge.final_project.org.AccountStatus;
import com.merge.final_project.org.Foundation;
import com.merge.final_project.org.FoundationRepository;
import com.merge.final_project.user.users.User;
import com.merge.final_project.user.users.UserRepository;
import com.merge.final_project.wallet.repository.WalletRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentDonationTest {

    @InjectMocks
    private PaymentServiceImpl paymentService;

    @Mock private PaymentRepository paymentRepository;
    @Mock private DonationRepository donationRepository;
    @Mock private TossPaymentClient tossPaymentClient;
    @Mock private CampaignRepository campaignRepository;
    @Mock private FoundationRepository foundationRepository;
    @Mock private UserRepository userRepository;
    @Mock private WalletRepository walletRepository;

    private User user;
    private Campaign campaign;
    private Payment payment;
    private Foundation foundation;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .userNo(1L)
                .build();

        campaign = Campaign.builder()
                .campaignNo(216L)
                .title("테스트 캠페인")
                .campaignStatus(CampaignStatus.ACTIVE)
                .startAt(LocalDateTime.now().minusDays(1))
                .endAt(LocalDateTime.now().plusDays(1))
                .foundationNo(50L)
                .currentAmount(BigDecimal.ZERO)
                .build();

        payment = Payment.builder()
                .paymentNo(10L)
                .userNo(1L)
                .campaignNo(216L)
                .orderKey("order-1")
                .amount(new BigDecimal("1000"))
                .method(PaymentMethod.CARD)
                .paymentStatus(PaymentStatus.READY)
                .isAnonymous(false)
                .privateKeyNo(1L)
                .build();

        foundation = Foundation.builder()
                .foundationNo(50L)
                .accountStatus(AccountStatus.ACTIVE)
                .build();
    }

    private PaymentReadyRequest readyRequest(Long campaignNo, BigDecimal amount, Boolean isAnonymous, PaymentMethod method) {
        PaymentReadyRequest request = new PaymentReadyRequest();
        ReflectionTestUtils.setField(request, "campaignNo", campaignNo);
        ReflectionTestUtils.setField(request, "amount", amount);
        ReflectionTestUtils.setField(request, "isAnonymous", isAnonymous);
        ReflectionTestUtils.setField(request, "method", method);
        return request;
    }

    private PaymentConfirmRequest confirmRequest(String paymentKey, String orderId, BigDecimal amount, PaymentMethod method) {
        PaymentConfirmRequest request = new PaymentConfirmRequest();
        ReflectionTestUtils.setField(request, "paymentKey", paymentKey);
        ReflectionTestUtils.setField(request, "orderId", orderId);
        ReflectionTestUtils.setField(request, "amount", amount);
        ReflectionTestUtils.setField(request, "method", method);
        return request;
    }

    private PaymentBody paymentBody(String paymentKey, BigDecimal totalAmount, String method) {
        PaymentBody body = mock(PaymentBody.class);
        given(body.getPaymentKey()).willReturn(paymentKey);
        given(body.getTotalAmount()).willReturn(totalAmount);
        given(body.getMethod()).willReturn(method);
        given(body.getApprovedAt()).willReturn(OffsetDateTime.now());
        return body;
    }

    @Nested
    @DisplayName("결제 준비(paymentReady)")
    class PaymentReadyTest {

        @Test
        @DisplayName("실패: 캠페인이 존재하지 않으면 예외가 발생한다")
        void ready_CampaignNotFound() {
            PaymentReadyRequest request = readyRequest(999L, new BigDecimal("1000"), false, PaymentMethod.CARD);
            given(campaignRepository.findById(999L)).willReturn(Optional.empty());

            BusinessException ex = assertThrows(
                    BusinessException.class,
                    () -> paymentService.paymentReady(1L, request)
            );

            assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.CAMPAIGN_NOT_FOUND);
        }

        @Test
        @DisplayName("실패: 캠페인이 ACTIVE 상태가 아니면 예외가 발생한다")
        void ready_CampaignNotActive() {
            campaign.setCampaignStatus(CampaignStatus.ENDED);
            PaymentReadyRequest request = readyRequest(216L, new BigDecimal("1000"), false, PaymentMethod.CARD);
            given(campaignRepository.findById(216L)).willReturn(Optional.of(campaign));

            BusinessException ex = assertThrows(
                    BusinessException.class,
                    () -> paymentService.paymentReady(1L, request)
            );

            assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.CAMPAIGN_NOT_ACTIVE);
        }

        @Test
        @DisplayName("실패: 기부 금액이 0원이면 예외가 발생한다")
        void ready_InvalidAmount() {
            PaymentReadyRequest request = readyRequest(216L, BigDecimal.ZERO, false, PaymentMethod.CARD);
            given(campaignRepository.findById(216L)).willReturn(Optional.of(campaign));

            BusinessException ex = assertThrows(
                    BusinessException.class,
                    () -> paymentService.paymentReady(1L, request)
            );

            assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.INVALID_DONATION_AMOUNT);
        }

        @Test
        @DisplayName("성공: 결제 준비 데이터가 저장되고 응답이 반환된다")
        void ready_Success() {
            PaymentReadyRequest request = readyRequest(216L, new BigDecimal("1000"), true, PaymentMethod.CARD);
            given(campaignRepository.findById(216L)).willReturn(Optional.of(campaign));
            given(paymentRepository.save(any(Payment.class))).willAnswer(invocation -> {
                Payment saved = invocation.getArgument(0);
                saved.setPaymentNo(10L);
                return saved;
            });

            PaymentReadyResponse response = paymentService.paymentReady(1L, request);

            assertThat(response.getPaymentNo()).isEqualTo(10L);
            assertThat(response.getAmount()).isEqualByComparingTo("1000");
            assertThat(response.getOrderName()).isEqualTo("테스트 캠페인");
            assertThat(response.getOrderId()).startsWith("DONATION-");

            verify(paymentRepository, times(1)).save(any(Payment.class));
        }
    }

    @Nested
    @DisplayName("결제 승인(confirmPayment)")
    class ConfirmPaymentTest {

        @Test
        @DisplayName("실패: 유저가 존재하지 않으면 예외가 발생한다")
        void confirm_UserNotFound() {
            PaymentConfirmRequest request = confirmRequest("pk-1", "order-1", new BigDecimal("1000"), PaymentMethod.CARD);
            given(userRepository.findById(1L)).willReturn(Optional.empty());

            BusinessException ex = assertThrows(
                    BusinessException.class,
                    () -> paymentService.confirmPayment(1L, request)
            );

            assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.USER_NOT_FOUND);
        }

        @Test
        @DisplayName("실패: 결제 내역이 존재하지 않으면 예외가 발생한다")
        void confirm_PaymentNotFound() {
            PaymentConfirmRequest request = confirmRequest("pk-1", "order-1", new BigDecimal("1000"), PaymentMethod.CARD);
            given(userRepository.findById(1L)).willReturn(Optional.of(user));
            given(paymentRepository.findByOrderKeyAndUserNo("order-1", 1L)).willReturn(Optional.empty());

            BusinessException ex = assertThrows(
                    BusinessException.class,
                    () -> paymentService.confirmPayment(1L, request)
            );

            assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.PAYMENT_NOT_FOUND);
        }

        @Test
        @DisplayName("실패: 결제 요청 금액과 DB 저장 금액이 다르면 승인되지 않는다")
        void confirm_AmountMismatch() {
            PaymentConfirmRequest request = confirmRequest("pk-1", "order-1", new BigDecimal("5000"), PaymentMethod.CARD);

            given(userRepository.findById(1L)).willReturn(Optional.of(user));
            given(paymentRepository.findByOrderKeyAndUserNo("order-1", 1L)).willReturn(Optional.of(payment));

            BusinessException ex = assertThrows(
                    BusinessException.class,
                    () -> paymentService.confirmPayment(1L, request)
            );

            assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.PAYMENT_AMOUNT_MISMATCH);
        }

        @Test
        @DisplayName("실패: 결제 수단이 다르면 예외가 발생한다")
        void confirm_MethodMismatch() {
            PaymentConfirmRequest request = confirmRequest("pk-1", "order-1", new BigDecimal("1000"), PaymentMethod.EASY_PAY);

            given(userRepository.findById(1L)).willReturn(Optional.of(user));
            given(paymentRepository.findByOrderKeyAndUserNo("order-1", 1L)).willReturn(Optional.of(payment));

            BusinessException ex = assertThrows(
                    BusinessException.class,
                    () -> paymentService.confirmPayment(1L, request)
            );

            assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.PAYMENT_METHOD_MISMATCH);
        }

        @Test
        @DisplayName("실패: 이미 처리된 기부면 예외가 발생한다")
        void confirm_DuplicateDonation() {
            PaymentConfirmRequest request = confirmRequest("pk-1", "order-1", new BigDecimal("1000"), PaymentMethod.CARD);

            given(userRepository.findById(1L)).willReturn(Optional.of(user));
            given(paymentRepository.findByOrderKeyAndUserNo("order-1", 1L)).willReturn(Optional.of(payment));
            given(campaignRepository.findById(216L)).willReturn(Optional.of(campaign));
            given(foundationRepository.findById(50L)).willReturn(Optional.of(foundation));
            given(donationRepository.existsByPaymentNo(10L)).willReturn(true);

            BusinessException ex = assertThrows(
                    BusinessException.class,
                    () -> paymentService.confirmPayment(1L, request)
            );

            assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.DUPLICATE_DONATION);
        }

        @Test
        @DisplayName("실패: 토스 승인 API 호출 실패 시 예외가 발생한다")
        void confirm_TossConfirmFailed() {
            PaymentConfirmRequest request = confirmRequest("pk-1", "order-1", new BigDecimal("1000"), PaymentMethod.CARD);

            given(userRepository.findById(1L)).willReturn(Optional.of(user));
            given(paymentRepository.findByOrderKeyAndUserNo("order-1", 1L)).willReturn(Optional.of(payment));
            given(campaignRepository.findById(216L)).willReturn(Optional.of(campaign));
            given(foundationRepository.findById(50L)).willReturn(Optional.of(foundation));
            given(donationRepository.existsByPaymentNo(10L)).willReturn(false);
            given(tossPaymentClient.confirmPayment(any(PaymentConfirmRequest.class)))
                    .willThrow(new BusinessException(ErrorCode.PAYMENT_CONFIRM_FAILED));

            BusinessException ex = assertThrows(
                    BusinessException.class,
                    () -> paymentService.confirmPayment(1L, request)
            );

            assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.PAYMENT_CONFIRM_FAILED);
        }

        @Test
        @DisplayName("실패: 토스 승인 후 서버 내부 에러가 나면 자동 취소된다")
        void confirm_InternalError_TriggersCancel() {
            PaymentConfirmRequest request = confirmRequest("pk-1", "order-1", new BigDecimal("1000"), PaymentMethod.CARD);
            PaymentBody body = paymentBody("fake-key", new BigDecimal("1000"), "카드");

            given(userRepository.findById(1L)).willReturn(Optional.of(user));
            given(paymentRepository.findByOrderKeyAndUserNo("order-1", 1L)).willReturn(Optional.of(payment));
            given(campaignRepository.findById(216L)).willReturn(Optional.of(campaign));
            given(foundationRepository.findById(50L)).willReturn(Optional.of(foundation));
            given(donationRepository.existsByPaymentNo(10L)).willReturn(false);
            given(paymentRepository.existsByPaymentKey("fake-key")).willReturn(false);
            given(tossPaymentClient.confirmPayment(any(PaymentConfirmRequest.class))).willReturn(body);

            doThrow(new RuntimeException("DB error")).when(donationRepository).save(any(Donation.class));

            BusinessException ex = assertThrows(
                    BusinessException.class,
                    () -> paymentService.confirmPayment(1L, request)
            );

            assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.DONATION_CREATE_FAILED);
            verify(tossPaymentClient, times(1))
                    .cancelPayment(eq("fake-key"), contains("서버 내부 오류"));
            assertThat(payment.getPaymentStatus()).isEqualTo(PaymentStatus.FAILED);
        }

        @Test
        @DisplayName("성공: 결제가 승인되고 기부가 생성된다")
        void confirm_Success() {
            PaymentConfirmRequest request = confirmRequest("pk-1", "order-1", new BigDecimal("1000"), PaymentMethod.CARD);
            PaymentBody body = paymentBody("fake-key", new BigDecimal("1000"), "카드");

            given(userRepository.findById(1L)).willReturn(Optional.of(user));
            given(paymentRepository.findByOrderKeyAndUserNo("order-1", 1L)).willReturn(Optional.of(payment));
            given(campaignRepository.findById(216L)).willReturn(Optional.of(campaign));
            given(foundationRepository.findById(50L)).willReturn(Optional.of(foundation));
            given(donationRepository.existsByPaymentNo(10L)).willReturn(false);
            given(paymentRepository.existsByPaymentKey("fake-key")).willReturn(false);
            given(tossPaymentClient.confirmPayment(any(PaymentConfirmRequest.class))).willReturn(body);
            given(donationRepository.save(any(Donation.class))).willAnswer(invocation -> invocation.getArgument(0));

            PaymentConfirmResponse response = paymentService.confirmPayment(1L, request);

            assertThat(response.getPaymentNo()).isEqualTo(10L);
            assertThat(response.getOrderId()).isEqualTo("order-1");
            assertThat(response.getPaymentKey()).isEqualTo("fake-key");
            assertThat(response.getAmount()).isEqualByComparingTo("1000");
            assertThat(response.getStatus()).isEqualTo("SUCCESS");

            assertThat(payment.getPaymentStatus()).isEqualTo(PaymentStatus.DONE);
            assertThat(payment.getPaymentKey()).isEqualTo("fake-key");
            assertThat(campaign.getCurrentAmount()).isEqualByComparingTo("1000");
        }
    }
}
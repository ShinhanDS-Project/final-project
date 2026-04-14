package com.merge.final_project.blockchain.payment;

import com.merge.final_project.blockchain.dto.BlockchainTransferResponse;
import com.merge.final_project.blockchain.payment.event.PaymentConfirmedEvent;
import com.merge.final_project.blockchain.service.BlockchainTransferService;
import com.merge.final_project.donation.donations.Donation;
import com.merge.final_project.donation.donations.DonationRepository;
import com.merge.final_project.donation.donations.DonationTokenStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.math.BigDecimal;
import java.util.List;

/**
 * 선우 작성:
 * 결제 성공 이후의 블록체인 연계를 담당하는 오케스트레이터.
 *
 * 설계 포인트:
 * 1. 결제 트랜잭션과 온체인 트랜잭션을 분리해 결제 성공 응답 지연을 줄인다.
 * 2. 상태 전이(낙관적 update) 기반으로 중복 실행/경합을 제어한다.
 * 3. 스케줄러 재시도로 일시 장애를 복구한다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class PaymentTokenTransferOrchestrator {

    private final DonationRepository donationRepository;
    private final BlockchainTransferService blockchainTransferService;

    /**
     * 결제 확정 이벤트를 커밋 이후 비동기로 수신한다.
     * 이벤트 발생 트랜잭션이 롤백되면 실행되지 않게 AFTER_COMMIT을 강제한다.
     */
    @Async
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onPaymentConfirmed(PaymentConfirmedEvent event) {
        if (event == null || event.donationNo() == null) {
            return;
        }
        processDonationChain(event.donationNo());
    }

    /**
     * 선우 작성:
     * 실패/중단된 건을 주기적으로 다시 잡아 처리한다.
     * - PENDING/FAILED_CHARGE: 충전 단계부터 재시도
     * - CHARGED/FAILED_DONATION: 기부 전송 단계부터 재시도
     */
    @Scheduled(fixedDelayString = "${donation.token.retry-interval-ms:30000}")
    public void retryPendingAndFailedTransfers() {
        List<String> retryableStatuses = List.of(
                DonationTokenStatus.PENDING.name(),
                DonationTokenStatus.FAILED_CHARGE.name(),
                DonationTokenStatus.CHARGED.name(),
                DonationTokenStatus.FAILED_DONATION.name()
        );
        List<Donation> targets = donationRepository.findTop50ByTokenStatusInOrderByDonationNoAsc(retryableStatuses);
        for (Donation donation : targets) {
            processDonationChain(donation.getDonationNo());
        }
    }

    /**
     * 상태를 기준으로 현재 donation이 진행해야 할 단계를 결정한다.
     */
    public void processDonationChain(Long donationNo) {
        if (donationNo == null) {
            return;
        }
        if (moveToCharging(donationNo)) {
            executeCharge(donationNo);
            return;
        }
        if (moveToDonating(donationNo)) {
            executeDonation(donationNo);
        }
    }

    /**
     * PENDING 또는 FAILED_CHARGE 상태를 CHARGING으로 원자 전이한다.
     */
    private boolean moveToCharging(Long donationNo) {
        int fromPending = donationRepository.updateTokenStatusIfCurrent(
                donationNo,
                DonationTokenStatus.PENDING.name(),
                DonationTokenStatus.CHARGING.name()
        );
        if (fromPending > 0) {
            return true;
        }
        int fromFailed = donationRepository.updateTokenStatusIfCurrent(
                donationNo,
                DonationTokenStatus.FAILED_CHARGE.name(),
                DonationTokenStatus.CHARGING.name()
        );
        return fromFailed > 0;
    }

    /**
     * CHARGED 또는 FAILED_DONATION 상태를 DONATING으로 원자 전이한다.
     */
    private boolean moveToDonating(Long donationNo) {
        int fromCharged = donationRepository.updateTokenStatusIfCurrent(
                donationNo,
                DonationTokenStatus.CHARGED.name(),
                DonationTokenStatus.DONATING.name()
        );
        if (fromCharged > 0) {
            return true;
        }
        int fromFailed = donationRepository.updateTokenStatusIfCurrent(
                donationNo,
                DonationTokenStatus.FAILED_DONATION.name(),
                DonationTokenStatus.DONATING.name()
        );
        return fromFailed > 0;
    }

    /**
     * 서버->사용자 토큰 충전을 실행하고 결과 상태를 기록한다.
     */
    private void executeCharge(Long donationNo) {
        try {
            Donation donation = donationRepository.findById(donationNo)
                    .orElseThrow(() -> new IllegalStateException("donation not found: " + donationNo));
            long amount = resolveAmount(donation.getDonationAmount(), donationNo);

            BlockchainTransferResponse response = blockchainTransferService.chargeUserToken(
                    donation.getUserNo(),
                    amount,
                    donationNo
            );
            if (!isSuccess(response)) {
                donationRepository.updateStatus(donationNo, DonationTokenStatus.FAILED_CHARGE.name());
                log.warn("token charge failed. donationNo={}, status={}, txHash={}",
                        donationNo,
                        response == null ? null : response.status(),
                        response == null ? null : response.txHash());
                return;
            }

            donationRepository.updateStatus(donationNo, DonationTokenStatus.CHARGED.name());
            if (moveToDonating(donationNo)) {
                executeDonation(donationNo);
            }
        } catch (Exception e) {
            donationRepository.updateStatus(donationNo, DonationTokenStatus.FAILED_CHARGE.name());
            log.warn("token charge exception. donationNo={}, reason={}", donationNo, e.getMessage());
        }
    }

    /**
     * 사용자->캠페인 토큰 전송을 실행하고 성공 시 DONE으로 완료 처리한다.
     */
    private void executeDonation(Long donationNo) {
        try {
            Donation donation = donationRepository.findById(donationNo)
                    .orElseThrow(() -> new IllegalStateException("donation not found: " + donationNo));
            long amount = resolveAmount(donation.getDonationAmount(), donationNo);

            BlockchainTransferResponse response = blockchainTransferService.transferDonationToCampaign(
                    donation.getUserNo(),
                    donation.getCampaignNo(),
                    amount,
                    donationNo
            );
            if (!isSuccess(response)) {
                donationRepository.updateStatus(donationNo, DonationTokenStatus.FAILED_DONATION.name());
                log.warn("token donation transfer failed. donationNo={}, status={}, txHash={}",
                        donationNo,
                        response == null ? null : response.status(),
                        response == null ? null : response.txHash());
                return;
            }

            donationRepository.updateStatusAndTransactionNo(
                    donationNo,
                    DonationTokenStatus.DONE.name(),
                    parseTransactionNo(response.transactionNo())
            );
        } catch (Exception e) {
            donationRepository.updateStatus(donationNo, DonationTokenStatus.FAILED_DONATION.name());
            log.warn("token donation transfer exception. donationNo={}, reason={}", donationNo, e.getMessage());
        }
    }

    /**
     * DB 금액(BigDecimal)을 블록체인 전송용 long으로 엄격 변환한다.
     */
    private long resolveAmount(BigDecimal amount, Long donationNo) {
        if (amount == null || amount.signum() <= 0) {
            throw new IllegalStateException("invalid donation amount. donationNo=" + donationNo);
        }
        try {
            return amount.longValueExact();
        } catch (ArithmeticException e) {
            throw new IllegalStateException("donation amount is not an exact long value. donationNo=" + donationNo, e);
        }
    }

    private boolean isSuccess(BlockchainTransferResponse response) {
        return response != null && "SUCCESS".equalsIgnoreCase(response.status());
    }

    /**
     * transactionNo는 외부 응답 스펙상 문자열이므로 파싱 실패를 방어한다.
     */
    private Long parseTransactionNo(String transactionNo) {
        if (transactionNo == null || transactionNo.isBlank()) {
            return null;
        }
        try {
            return Long.parseLong(transactionNo);
        } catch (NumberFormatException e) {
            log.warn("invalid transactionNo format. value={}", transactionNo);
            return null;
        }
    }
}

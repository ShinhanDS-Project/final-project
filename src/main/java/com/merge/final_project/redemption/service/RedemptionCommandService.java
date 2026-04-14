package com.merge.final_project.redemption.service;

import com.merge.final_project.blockchain.entity.Transaction;
import com.merge.final_project.redemption.RequesterType;
import com.merge.final_project.redemption.entity.Redemption;
import com.merge.final_project.redemption.repository.RedemptionRepository;
import com.merge.final_project.wallet.entity.Wallet;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RedemptionCommandService {

    private final RedemptionRepository redemptionRepository;

    // 환급 요청 생성 (PENDING)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Redemption createPending(RequesterType requesterType, Long requesterNo, Long amount, Wallet wallet) {
        return redemptionRepository.save(
                Redemption.create(requesterType, requesterNo, amount, wallet)
        );
    }

    // 처리 시작 (PROCESSING)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markProcessing(Long redemptionNo) {
        Redemption redemption = redemptionRepository.findById(redemptionNo)
                .orElseThrow(() -> new IllegalArgumentException("redemption not found"));

        redemption.markProcessing();
    }

    // 온체인 환급 성공 직후 상태 전환 (PROCESSING -> ONCHAIN_CONFIRMED)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markOnChainConfirmed(Long redemptionNo, Long blockNumber) {
        Redemption redemption = redemptionRepository.findById(redemptionNo)
                .orElseThrow(() -> new IllegalArgumentException("redemption not found"));

        redemption.markOnChainConfirmed(blockNumber);
    }

    // 온체인 자체 실패 처리 (FAILED)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markFailed(Long redemptionNo, String failureReason) {
        Redemption redemption = redemptionRepository.findById(redemptionNo)
                .orElseThrow(() -> new IllegalArgumentException("redemption not found"));

        redemption.markFailed(failureReason);
    }

    // 최종 완료 처리 (COMPLETED)
    @Transactional
    public void markCompleted(Long redemptionNo, Transaction transaction, Long blockNumber) {
        Redemption redemption = redemptionRepository.findById(redemptionNo)
                .orElseThrow(() -> new IllegalArgumentException("redemption not found"));

        redemption.markCompleted(transaction, blockNumber);
    }

    // 실제 현금 지급 완료 (PAID)
    @Transactional
    public void markPaid(Long redemptionNo) {
        Redemption redemption = redemptionRepository.findById(redemptionNo)
                .orElseThrow(() -> new IllegalArgumentException("redemption not found"));

        redemption.markCashPaid();
    }
}

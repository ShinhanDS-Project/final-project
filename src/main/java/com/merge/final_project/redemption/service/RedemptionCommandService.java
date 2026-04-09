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

    // 현금화 요청 생성 (PENDING)
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
                .orElseThrow(() -> new IllegalArgumentException("현금화 요청 정보를 찾을 수 없습니다."));

        redemption.markProcessing();
    }
    // 실패 처리 (FAILED)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markFailed(Long redemptionNo, String failureReason) {
        Redemption redemption = redemptionRepository.findById(redemptionNo)
                .orElseThrow(() -> new IllegalArgumentException("현금화 요청 정보를 찾을 수 없습니다."));

        redemption.markFailed(failureReason);
    }
    // 온체인 성공 → 완료 처리 (COMPLETED)
    @Transactional
    public void markCompleted(Long redemptionNo, Transaction transaction, Long blockNumber) {
        Redemption redemption = redemptionRepository.findById(redemptionNo)
                .orElseThrow(() -> new IllegalArgumentException("현금화 요청 정보를 찾을 수 없습니다."));

        redemption.markCompleted(transaction, blockNumber);
    }
    // 실제 현금 지급 완료 (PAID)
    @Transactional
    public void markPaid(Long redemptionNo) {
        Redemption redemption = redemptionRepository.findById(redemptionNo)
                .orElseThrow(() -> new IllegalArgumentException("현금화 요청 정보를 찾을 수 없습니다."));

        redemption.markCashPaid();
    }
}

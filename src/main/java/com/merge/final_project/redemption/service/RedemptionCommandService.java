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

    // 현금화 요청 원본은 온체인 처리와 분리해서 먼저 저장해야
    // 체인 호출 자체가 실패해도 어떤 요청이 실패했는지 추적할 수 있다.
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Redemption createPending(RequesterType requesterType, Long requesterNo, Long amount, Wallet wallet) {
        return redemptionRepository.save(
                Redemption.create(requesterType, requesterNo, amount, wallet)
        );
    }

    // 실제 체인 호출 직전에 PROCESSING 으로 바꿔두면
    // 이후 로컬 후처리 실패를 PENDING 과 구분해서 볼 수 있다.
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markProcessing(Long redemptionNo) {
        Redemption redemption = redemptionRepository.findById(redemptionNo)
                .orElseThrow(() -> new IllegalArgumentException("현금화 요청 정보를 찾을 수 없습니다."));

        redemption.markProcessing();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markFailed(Long redemptionNo, String failureReason) {
        Redemption redemption = redemptionRepository.findById(redemptionNo)
                .orElseThrow(() -> new IllegalArgumentException("현금화 요청 정보를 찾을 수 없습니다."));

        redemption.markFailed(failureReason);
    }

    @Transactional
    public void markCompleted(Long redemptionNo, Transaction transaction, Long blockNumber) {
        Redemption redemption = redemptionRepository.findById(redemptionNo)
                .orElseThrow(() -> new IllegalArgumentException("현금화 요청 정보를 찾을 수 없습니다."));

        redemption.markCompleted(transaction, blockNumber);
    }
}

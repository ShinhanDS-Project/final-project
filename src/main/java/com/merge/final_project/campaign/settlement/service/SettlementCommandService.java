package com.merge.final_project.campaign.settlement.service;

import com.merge.final_project.campaign.campaigns.entity.Campaign;
import com.merge.final_project.campaign.settlement.Repository.SettlementRepository;
import com.merge.final_project.campaign.settlement.Settlement;
import com.merge.final_project.campaign.settlement.SettlementStatus;
import com.merge.final_project.org.Foundation;
import com.merge.final_project.recipient.beneficiary.entity.Beneficiary;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class SettlementCommandService {

    private final SettlementRepository settlementRepository;

    // 정산 생성 (초기 상태: PENDING)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Settlement createPendingSettlement(
            String transactionCode,
            Foundation foundation,
            Beneficiary beneficiary,
            Long totalAmount,
            Long foundationAmount,
            Long beneficiaryAmount,
            Campaign campaign
    ) {
        Settlement settlement = Settlement.builder()
                .transactionCode(transactionCode)
                .foundation(foundation)
                .beneficiary(beneficiary)
                .amount(totalAmount)
                .status(SettlementStatus.PENDING)
                .foundationAmount(foundationAmount)
                .beneficiaryAmount(beneficiaryAmount)
                .campaign(campaign)
                .build();

        return settlementRepository.save(settlement);
    }

    // 처리 시작 (PENDING -> PROCESSING)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markProcessing(Long settlementNo) {
        Settlement settlement = settlementRepository.findById(settlementNo)
                .orElseThrow(() -> new IllegalArgumentException("settlement not found"));

        settlement.markProcessing();
    }

    // 온체인 정산 성공 직후 상태 전환 (PROCESSING -> ONCHAIN_CONFIRMED)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markOnChainConfirmed(Long settlementNo) {
        Settlement settlement = settlementRepository.findById(settlementNo)
                .orElseThrow(() -> new IllegalArgumentException("settlement not found"));

        settlement.markOnChainConfirmed();
    }

    // 정산 최종 완료 처리 (-> COMPLETED)
    @Transactional
    public void markCompleted(Long settlementNo) {
        Settlement settlement = settlementRepository.findById(settlementNo)
                .orElseThrow(() -> new IllegalArgumentException("settlement not found"));
        settlement.setStatus(SettlementStatus.COMPLETED);
        settlement.setSettledAt(LocalDateTime.now());
    }

    // 온체인 자체 실패 처리 (-> FAILED)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markFailed(Long settlementNo) {
        Settlement settlement = settlementRepository.findById(settlementNo)
                .orElseThrow(() -> new IllegalArgumentException("settlement not found"));

        settlement.failed();
    }
}

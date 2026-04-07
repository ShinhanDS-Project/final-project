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

    //정산 생성
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Settlement createPendingSettlement(
            String transactionCode,
            Foundation foundation,
            Beneficiary beneficiary,
            Integer totalAmount,
            Integer foundationAmount,
            Integer beneficiaryAmount,
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

    //정산 완료 처리
    @Transactional
    public void markCompleted(Long settlementNo) {
        Settlement settlement = settlementRepository.findById(settlementNo)
                .orElseThrow(() -> new IllegalArgumentException("정산 없음"));
        settlement.setStatus(SettlementStatus.COMPLETED);
        settlement.setSettledAt(LocalDateTime.now());
    }

    //정산 실패 처리
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markFailed(Long settlementNo) {
        Settlement settlement = settlementRepository.findById(settlementNo)
                .orElseThrow(() -> new IllegalArgumentException("정산 없음"));

        settlement.failed();
    }
}

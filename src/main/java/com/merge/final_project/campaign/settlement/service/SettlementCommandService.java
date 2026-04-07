package com.merge.final_project.campaign.settlement.service;

import com.merge.final_project.campaign.campaigns.entity.Campaign;
import com.merge.final_project.campaign.settlement.Repository.SettlementRepository;
import com.merge.final_project.campaign.settlement.Settlement;
import com.merge.final_project.campaign.settlement.SettlementStatus;
import com.merge.final_project.org.foundation.Foundation;
import com.merge.final_project.recipient.beneficiary.Beneficiary;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class SettlementCommandService {

    private final SettlementRepository settlementRepository;

    // 정산 원본은 체인 처리와 분리해서 먼저 저장해야
    // 체인 호출 실패가 나도 어떤 정산 시도가 실패했는지 남길 수 있다.
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

    // 실제 온체인 정산을 시작하기 직전에 PROCESSING 으로 바꿔두면,
    // 체인 성공 후 로컬 저장만 실패한 건을 PENDING 과 구분해서 볼 수 있다.
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markProcessing(Long settlementNo) {
        Settlement settlement = settlementRepository.findById(settlementNo)
                .orElseThrow(() -> new IllegalArgumentException("정산 정보를 찾을 수 없습니다."));

        settlement.markProcessing();
    }

    @Transactional
    public void markCompleted(Long settlementNo) {
        Settlement settlement = settlementRepository.findById(settlementNo)
                .orElseThrow(() -> new IllegalArgumentException("정산 정보를 찾을 수 없습니다."));
        settlement.setStatus(SettlementStatus.COMPLETED);
        settlement.setSettledAt(LocalDateTime.now());
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markFailed(Long settlementNo) {
        Settlement settlement = settlementRepository.findById(settlementNo)
                .orElseThrow(() -> new IllegalArgumentException("정산 정보를 찾을 수 없습니다."));

        settlement.failed();
    }
}

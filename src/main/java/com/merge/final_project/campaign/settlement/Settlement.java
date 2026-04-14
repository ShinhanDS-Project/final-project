package com.merge.final_project.campaign.settlement;

import com.merge.final_project.campaign.campaigns.entity.Campaign;
import com.merge.final_project.org.Foundation;
import com.merge.final_project.recipient.beneficiary.entity.Beneficiary;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "settlement")
@Getter
@NoArgsConstructor
@Builder
@Setter
@AllArgsConstructor
public class Settlement {

    @Id
    @Column(name = "settlement_no")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long settlementNo;

    @Column(name = "transaction_code", length = 255)
    private String transactionCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "foundation_no", nullable = false)
    private Foundation foundation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "campaign_no", nullable = false)
    private Campaign campaign;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "beneficiary_no", nullable = false)
    private Beneficiary beneficiary;

    private Long amount;

    @Column(name = "settled_at")
    private LocalDateTime settledAt;

    @Column(name = "foundation_amount")
    private Long foundationAmount;

    @Column(name = "beneficiary_amount")
    private Long beneficiaryAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private SettlementStatus status;

    // 정산 생성 (초기 상태: PENDING)
    public Settlement(
            Foundation foundation,
            Campaign campaign,
            Beneficiary beneficiary,
            Long amount,
            Long foundationAmount,
            Long beneficiaryAmount
    ) {
        this.foundation = foundation;
        this.campaign = campaign;
        this.beneficiary = beneficiary;
        this.amount = amount;
        this.foundationAmount = foundationAmount;
        this.beneficiaryAmount = beneficiaryAmount;
        this.status = SettlementStatus.PENDING;
    }

    // 정산 실패 처리
    public void failed() {
        this.status = SettlementStatus.FAILED;
    }

    // 처리 시작 (PENDING -> PROCESSING)
    public void markProcessing() {
        this.status = SettlementStatus.PROCESSING;
    }

    // 온체인 정산은 성공했지만 로컬 후처리가 아직 남아 있는 상태
    public void markOnChainConfirmed() {
        this.status = SettlementStatus.ONCHAIN_CONFIRMED;
    }
}

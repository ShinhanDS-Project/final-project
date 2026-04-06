package com.merge.final_project.campaign.settlement;

import com.merge.final_project.blockchain.entity.Transaction;
import com.merge.final_project.campaign.campaigns.Campaign;
import com.merge.final_project.org.foundation.Foundation;
import com.merge.final_project.recipient.beneficiary.Beneficiary;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name="settlement")
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

    private Integer amount;

    @Column(name = "settled_at")
    private LocalDateTime settledAt;

    @Column(name = "foundation_amount")
    private Integer foundationAmount;

    @Column(name = "beneficiary_amount")
    private Integer beneficiaryAmount;

    @Enumerated(EnumType.STRING)
    @Column(name="status", nullable = false)
    private SettlementStatus status;

    public Settlement(
            Foundation foundation,
            Campaign campaign,
            Beneficiary beneficiary,
            Integer amount,
            Integer foundationAmount,
            Integer beneficiaryAmount
    ) {
        this.foundation = foundation;
        this.campaign = campaign;
        this.beneficiary = beneficiary;
        this.amount = amount;
        this.foundationAmount = foundationAmount;
        this.beneficiaryAmount = beneficiaryAmount;
        this.status = SettlementStatus.PENDING;
    }

    public void completed(Transaction transaction){
        this.transactionCode = transactionCode;
        this.status = SettlementStatus.COMPLETED;
        this.settledAt = LocalDateTime.now();
    }

    public void failed() {
        this.status = SettlementStatus.FAILED;
    }

    public void markProcessing() {
        this.status = SettlementStatus.PROCESSING;
    }


}

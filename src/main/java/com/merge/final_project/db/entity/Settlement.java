package com.merge.final_project.db.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity(name = "DbSettlement")
@Table(name = "settlement")
@Getter
@Setter
@NoArgsConstructor
public class Settlement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "settlement_no")
    private Integer settlementNo;

    @Column(name = "foundation_no")
    private Integer foundationNo;

    @Column(name = "amount")
    private BigDecimal amount;

    @Column(name = "status")
    private String status;

    @Column(name = "settled_at")
    private LocalDateTime settledAt;

    @Column(name = "foundation_amount")
    private Integer foundationAmount;

    @Column(name = "beneficiary_amount")
    private Integer beneficiaryAmount;

    @Column(name = "beneficiary_no", nullable = false)
    private Integer beneficiaryNo;

    @Column(name = "campaign_no", nullable = false)
    private Integer campaignNo;

    @Column(name = "transaction_code")
    private String transactionCode;
}

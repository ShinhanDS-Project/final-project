package com.merge.final_project.db.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity(name = "DbRedemption")
@Table(name = "redemption")
@Getter
@Setter
@NoArgsConstructor
public class Redemption {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "redemption_no")
    private Integer redemptionNo;

    @Column(name = "requester_type", nullable = false)
    private String requesterType;

    @Column(name = "amount", nullable = false)
    private Integer amount;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "requested_at", nullable = false)
    private LocalDateTime requestedAt;

    @Column(name = "cash_paid_at")
    private LocalDateTime cashPaidAt;

    @Column(name = "requester_no", nullable = false)
    private Integer requesterNo;

    @Column(name = "wallet_no", nullable = false)
    private Integer walletNo;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    @Column(name = "failure_reason")
    private String failureReason;

    @Column(name = "block_number")
    private Long blockNumber;

    @Column(name = "transaction_no")
    private Long transactionNo;
}

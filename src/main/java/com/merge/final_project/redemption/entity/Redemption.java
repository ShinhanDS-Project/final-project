package com.merge.final_project.redemption.entity;

import com.merge.final_project.blockchain.entity.Transaction;
import com.merge.final_project.redemption.RedemptionStatus;
import com.merge.final_project.redemption.RequesterType;
import com.merge.final_project.wallet.entity.Wallet;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "redemption")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor
public class Redemption {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "redemption_no")
    private Long redemptionNo;

    @Enumerated(EnumType.STRING)
    @Column(name = "requester_type", nullable = false, length = 255)
    private RequesterType requesterType;

    @Column(name = "amount", nullable = false)
    private Long amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private RedemptionStatus status;

    @Column(name = "requested_at", nullable = false)
    private LocalDateTime requestedAt;

    @Column(name = "cash_paid_at")
    private LocalDateTime cashPaidAt;

    @Column(name = "requester_no", nullable = false)
    private Long requesterNo;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    @Column(name = "failure_reason", columnDefinition = "text")
    private String failureReason;

    @Column(name = "block_number")
    private Long blockNumber;

    @Version
    @Column(name = "version")
    private Long version;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "wallet_no", nullable = false)
    private Wallet wallet;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transaction_no")
    private Transaction transaction;

    // 환급 요청 생성 (초기 상태: PENDING)
    public static Redemption create(
            RequesterType requesterType,
            long requesterNo,
            long amount,
            Wallet wallet
    ) {
        return Redemption.builder()
                .requesterType(requesterType)
                .requesterNo(requesterNo)
                .amount(amount)
                .wallet(wallet)
                .status(RedemptionStatus.PENDING)
                .requestedAt(LocalDateTime.now())
                .build();
    }

    // 온체인 호출 직전/진행 중 상태
    public void markProcessing() {
        this.status = RedemptionStatus.PROCESSING;
    }

    // 온체인 환급은 성공했지만 로컬 후처리가 아직 끝나지 않은 상태
    public void markOnChainConfirmed(Long blockNumber) {
        this.status = RedemptionStatus.ONCHAIN_CONFIRMED;
        this.blockNumber = blockNumber;
        this.failureReason = null;
    }

    // 최종 완료 처리 + 연결된 트랜잭션 저장
    public void markCompleted(Transaction transaction, Long blockNumber) {
        this.status = RedemptionStatus.COMPLETED;
        this.transaction = transaction;
        this.blockNumber = blockNumber;
        this.processedAt = LocalDateTime.now();
        this.failureReason = null;
    }

    // 온체인 자체 실패
    public void markFailed(String failureReason) {
        this.status = RedemptionStatus.FAILED;
        this.failureReason = failureReason;
        this.processedAt = LocalDateTime.now();
    }

    // 실제 현금 지급 완료
    public void markCashPaid() {
        this.status = RedemptionStatus.PAID;
        this.cashPaidAt = LocalDateTime.now();
    }
}

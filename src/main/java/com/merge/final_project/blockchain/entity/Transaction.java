package com.merge.final_project.blockchain.entity;

import com.merge.final_project.wallet.entity.Wallet;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "token_transaction")
@Getter
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "transaction_no")
    private Long transactionNo;

    @Column(name = "transaction_code", length = 255)
    private String transactionCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_wallet_no", nullable = false)
    private Wallet fromWallet;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_wallet_no", nullable = false)
    private Wallet toWallet;
    
    private Integer amount;

    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "tx_hash")
    private String txHash;

    @Column(name="block_num")
    private Long blockNum;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private TransactionStatus status;

    @Column(name = "gas_fee", precision = 19, scale = 8)
    private BigDecimal gasFee;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false)
    private TransactionEventType eventType;

    // 트랜잭션 생성 (초기 상태: PENDING)
    public Transaction(
            Wallet fromWallet,
            Wallet toWallet,
            Integer amount,
            TransactionEventType eventType
    ) {
        this.fromWallet = fromWallet;
        this.toWallet = toWallet;
        this.amount = amount;
        this.eventType = eventType;
        this.status = TransactionStatus.PENDING;
        this.createdAt = LocalDateTime.now();
    }

    // 트랜잭션 처리 중 상태로 변경 (온체인 실행 시작)
    public void markProcessing() {
        this.status = TransactionStatus.PROCESSING;
    }

    // 온체인 성공 후 완료 처리 + 해시/블록/가스 정보 저장
    public void complete(String txHash, Long blockNum, BigDecimal gasFee) {
        this.txHash = txHash;
        this.blockNum = blockNum;
        this.gasFee = gasFee;
        this.status = TransactionStatus.SUCCESS;
        this.sentAt = LocalDateTime.now();
    }

    // 온체인 실패 시 상태 변경
    public void fail() {
        this.status = TransactionStatus.FAILED;
    }


}

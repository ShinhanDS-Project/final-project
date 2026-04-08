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

import java.time.LocalDateTime;

@Entity(name = "DbTokenTransaction")
@Table(name = "token_transaction")
@Getter
@Setter
@NoArgsConstructor
public class TokenTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "transaction_no")
    private Long transactionNo;

    @Column(name = "from_wallet_no")
    private Integer fromWalletNo;

    @Column(name = "to_wallet_no")
    private Integer toWalletNo;

    @Column(name = "amount")
    private Integer amount;

    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    @Column(name = "tx_hash")
    private String txHash;

    @Column(name = "block_num")
    private Integer blockNum;

    @Column(name = "status")
    private String status;

    @Column(name = "gas_fee")
    private Integer gasFee;

    @Column(name = "event_type")
    private String eventType;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "transaction_code")
    private String transactionCode;
}

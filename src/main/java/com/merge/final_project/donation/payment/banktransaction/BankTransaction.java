package com.merge.final_project.donation.payment.banktransaction;

import com.merge.final_project.donation.payment.Payment;
import jakarta.persistence.*;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "bank_transaction")
@Getter
public class BankTransaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long bankTransactionNo;

    @Column(name = "to_account_no")
    private String toAccountNo;

    @Column(name = "amount")
    private BigDecimal amount;

    @Column(name = "transferred_at")
    private LocalDateTime transferredAt;

    @Column(name = "from_account_no")
    private String fromAccountNo;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_no")
    private Payment payment;
}

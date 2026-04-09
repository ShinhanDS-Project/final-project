package com.merge.final_project.donation.donations;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "donation")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Donation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long donationNo;

    @Column(name = "payment_no",nullable = false)
    private Long paymentNo;

    @Column(name="transaction_no")
    private Long transactionNo;


    @Column(name = "user_no")
    private Long userNo;

    @Column(name = "campaign_no")
    private Long campaignNo;

    //기부여부 -true -false
    @Column(name = "is_anonymous")
    private boolean isAnonymous;

    @Column(name = "donated_at")
    private LocalDateTime donatedAt;

    @Column(name = "donor_wallet_no")
    private Long donorWalletNo;


    @Column(name = "campaign_wallet_no")
    private Long campaignWalletNo;

    @Column(name = "donation_amount", nullable = false, precision = 15, scale = 0)
    private BigDecimal donationAmount;
}

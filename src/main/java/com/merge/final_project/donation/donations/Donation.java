package com.merge.final_project.donation.donations;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.sql.Timestamp;
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
    @Column(name = "donation_no")
    private Long donationNo;

    @Column(name = "payment_no",nullable = false, unique = true)
    private Long paymentNo;

    //선우가 연결 시켜줘야할듯 ~
//    @Column(name="transaction_no")
//    private Long transactionNo;


    @Column(name = "user_no", nullable = false)
    private Long userNo;

    @Column(name = "campaign_no", nullable = false)
    private Long campaignNo;

    //기부할 때 익명여부 -true -false
    @Column(name = "is_anonymous", nullable = false)
    private boolean isAnonymous;

    @Column(name = "donated_at", nullable = false)
    private LocalDateTime donatedAt;

    @Column(name = "donor_wallet_no", nullable = false)
    private Long donorWalletNo;

    //기부하면 토큰이 이동해야하기 때문에 null 불가
    @Column(name = "campaign_wallet_no", nullable = false)
    private Long campaignWalletNo;

    @Column(name = "donation_amount", nullable = false, precision = 15, scale = 0)
    private BigDecimal donationAmount;

    @Column(name="token_status")// null
    private String tokenStatus;

    @Column(name="transaction_no")
    private Long transactionNo;

    @CreationTimestamp
    @Column(name="created_at",nullable = false)
    private Timestamp createdAt;

    @Column(name = "key_no")
    private Long keyNo;

}

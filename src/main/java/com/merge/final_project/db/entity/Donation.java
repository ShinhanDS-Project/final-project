package com.merge.final_project.db.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity(name = "DbDonation")
@Table(name = "donation")
@Getter
@Setter
@NoArgsConstructor
public class Donation {

    @Id
    @Column(name = "donation_no", nullable = false)
    private String donationNo;

    @Column(name = "payment_no", nullable = false)
    private String paymentNo;

    @Column(name = "user_no", nullable = false)
    private String userNo;

    @Column(name = "is_anonymous")
    private String isAnonymous;

    @Column(name = "donated_at")
    private String donatedAt;

    @Column(name = "donor_wallet_no")
    private String donorWalletNo;

    @Column(name = "campaign_wallet_no")
    private String campaignWalletNo;

    @Column(name = "token_status")
    private String tokenStatus;

    @Column(name = "donation_amount")
    private String donationAmount;

    @Column(name = "key_no", nullable = false)
    private String keyNo;

    @Column(name = "campaign_no", nullable = false)
    private Integer campaignNo;

    @Column(name = "transaction_no")
    private Long transactionNo;
}

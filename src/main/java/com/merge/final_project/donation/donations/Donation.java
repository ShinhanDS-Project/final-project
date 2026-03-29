package com.merge.final_project.donation.donations;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "donation")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Donation {

    @Id
    @Column(name = "donation_no")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long donationNo;

    @Column(name = "payment_no")
    private Long paymentNo;

    @Column(name = "user_no")
    private Long userNo;

    @Column(name = "campaign_no")
    private Long campaignNo;

    @Column(name = "is_anonymous")
    private String isAnonymous;

    @Column(name = "donated_at")
    private LocalDateTime donatedAt;

    @Column(name = "wallet_no")
    private Long walletNo;
}

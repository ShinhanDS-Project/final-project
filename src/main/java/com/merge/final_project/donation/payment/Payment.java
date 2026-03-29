package com.merge.final_project.donation.payment;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "payment")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {

    @Id
    @Column(name = "payment_no")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long paymentNo;

    private String method;

    @Enumerated(EnumType.STRING)
    private PaymentStatus status;

    private String amount;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @Column(name = "payment_key")
    private String paymentKey;

    @Column(name = "order_key")
    private String orderKey;

    @Column(name = "campaign_no")
    private Long campaignNo;

    @Column(name = "user_no")
    private Long userNo;
}

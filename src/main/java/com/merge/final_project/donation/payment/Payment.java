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
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer paymentNo;

    @Column(name="payment_method")
    private String payment_method; //결제수단 : 계좌이체 / 페이 / 카드 결제 -> enum 만들어주세요

   @Column(name="payment_status")
    private PaymentStatus status;

   @Column(name="amount")
    private String amount;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @Column(name = "payment_key",unique = true)
    private String paymentKey;

    @Column(name = "order_key",unique = true)
    private String orderKey;

    @Column(name = "campaign_no")
    private Long campaignNo;

    @Column(name = "user_no")
    private Long userNo;
}

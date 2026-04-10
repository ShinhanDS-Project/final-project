package com.merge.final_project.donation.payment;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payment")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long paymentNo;

    @Enumerated(EnumType.STRING)
    @Column(name="payment_method", nullable = false)
    private PaymentMethod method; //결제수단 : / 페이 / 카드 결제 -> enum 만들어주세요

    @Enumerated(EnumType.STRING)
   @Column(name="payment_status", nullable = false)
    private PaymentStatus paymentStatus;

   @Column(name="amount", nullable = false, precision = 15, scale = 0)
    private BigDecimal amount;

    @CreationTimestamp
    @Column(name = "created_at",nullable = false)
    private LocalDateTime createdAt;


    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @Column(name = "payment_key",unique = true)
    private String paymentKey;

    @Column(name="privatekey_no",nullable = false)
    private Long privateKeyNo;

    @Column(name = "order_key",unique = true)
    private String orderKey;

    @Column(name = "campaign_no", nullable = false)
    private Long campaignNo;

    @Column(name = "user_no", nullable = false)
    private Long userNo;

    @Column(name = "is_anonymous", nullable = false)
    private Boolean isAnonymous;

}

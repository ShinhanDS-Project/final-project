package com.merge.final_project.db.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity(name = "DbPayment")
@Table(name = "payment")
@Getter
@Setter
@NoArgsConstructor
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payment_no")
    private Integer paymentNo;

    @Column(name = "payment_method", nullable = false)
    private String paymentMethod;

    @Column(name = "payment_status", nullable = false)
    private String paymentStatus;

    @Column(name = "amount", nullable = false)
    private Long amount;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @Column(name = "payment_key")
    private String paymentKey;

    @Column(name = "order_key", nullable = false)
    private String orderKey;

    @Column(name = "user_no", nullable = false)
    private Integer userNo;

    @Column(name = "privatekey_no", nullable = false)
    private Integer privatekeyNo;

    @Column(name = "campaign_no", nullable = false)
    private Integer campaignNo;
}

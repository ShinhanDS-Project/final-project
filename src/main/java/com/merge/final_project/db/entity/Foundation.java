package com.merge.final_project.db.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity(name = "DbFoundation")
@Table(name = "foundation")
@Getter
@Setter
@NoArgsConstructor
public class Foundation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "foundation_no")
    private Integer foundationNo;

    @Column(name = "foundation_email", nullable = false)
    private String foundationEmail;

    @Column(name = "foundation_password")
    private String foundationPassword;

    @Column(name = "foundation_name")
    private String foundationName;

    @Column(name = "representative_name")
    private String representativeName;

    @Column(name = "business_registration_number")
    private String businessRegistrationNumber;

    @Column(name = "description")
    private String description;

    @Column(name = "profile_path")
    private String profilePath;

    @Column(name = "contact_phone")
    private String contactPhone;

    @Column(name = "account")
    private String account;

    @Column(name = "fee_rate")
    private BigDecimal feeRate;

    @Column(name = "foundation_hash")
    private String foundationHash;

    @Column(name = "account_status")
    private String accountStatus;

    @Column(name = "review_status")
    private String reviewStatus;

    @Column(name = "reject_reason")
    private String rejectReason;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "wallet_no")
    private Integer walletNo;

    @Column(name = "campaign_wallet1")
    private String campaignWallet1;

    @Column(name = "campaign_wallet2")
    private String campaignWallet2;

    @Column(name = "campaign_wallet3")
    private String campaignWallet3;

    @Column(name = "foundation_type")
    private String foundationType;

    @Column(name = "bank_name")
    private String bankName;
}

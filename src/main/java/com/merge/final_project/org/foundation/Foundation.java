package com.merge.final_project.org.foundation;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "foundation")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Foundation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "foundation_no")
    private Long foundationNo;

    @Column(name = "foundation_email", nullable = false)
    private String foundationEmail;

    @Column(name = "foundation_password")
    private String foundationPassword;

    @Column(name = "foundation_name")
    private String foundationName;

    @Column(name = "business_registration_number")
    private String businessRegistrationNumber;

    @Column(name = "representative_name")
    private String representativeName;

    @Column(name = "contact_phone")
    private String contactPhone;

    @Column(name = "description")
    private String description;

    @Column(name = "fee_rate")
    private BigDecimal feeRate;

    @Column(name = "foundation_hash")
    private String foundationHash;

    @Column(name = "account_status")
    private String accountStatus;

    @Column(name = "review_status")
    private String reviewStatus;

    @Column(name = "reject_reason", columnDefinition = "TEXT")
    private String rejectReason;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "profile_path")
    private String profilePath;

    @Column(name = "campaign_wallet1")
    private String campaignWallet1;

    @Column(name = "campaign_wallet2")
    private String campaignWallet2;

    @Column(name = "campaign_wallet3")
    private String campaignWallet3;

//    @OneToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "login_no", nullable = false)
//    private UsersAccount usersAccount;

    @Column(name = "account")
    private String account;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "wallet_no", nullable = false)
    private Wallet wallet;
}

package com.merge.final_project.org.foundation;

import com.merge.final_project.auth.useraccount.UsersAccount;
import com.merge.final_project.wallet.Wallet;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "foundation")
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
    private Integer businessRegistrationNumber;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "profile_path")
    private String profilePath;

    @Column(name = "contact_phone")
    private String contactPhone;

    private String account;

    @Column(name = "fee_rate")
    private Integer feeRate;

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

    private String campaignWallet1;
    private String campaignWallet2;
    private String campaignWallet3;
}

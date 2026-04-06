package com.merge.final_project.org;

import com.merge.final_project.auth.useraccount.UsersAccount;
import com.merge.final_project.wallet.Wallet;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "foundation")
@Getter
@NoArgsConstructor
public class Foundation {

    @Id
    @Column(name = "foundation_no")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long foundationNo;

    @Column(name = "foundation_hash")
    private String foundationHash;

    @Column(name = "foundation_name")
    private String foundationName;

    @Column(name = "business_registration_number")
    private String businessRegistrationNumber;

    @Column(name = "representative_name")
    private String representativeName;

    @Column(name = "contact_phone")
    private String contactPhone;

    @Column(name = "fee_rate")
    private BigDecimal feeRate;

    @Column(name = "image_path")
    private String imagePath;

    private String description;
    private String account;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "login_no", nullable = false)
    private UsersAccount usersAccount;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "wallet_no", nullable = false)
    private Wallet wallet;
}

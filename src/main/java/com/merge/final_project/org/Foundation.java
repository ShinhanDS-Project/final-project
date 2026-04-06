package com.merge.final_project.org;

import com.merge.final_project.global.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "foundation")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Foundation extends BaseEntity {

    @Id
    @Column(name = "foundation_no")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long foundationNo;

    @Column(name = "foundation_email")
    private String foundationEmail;

    @Column(name = "foundation_password")
    private String foundationPassword;

    @Column(name = "foundation_name")
    private String foundationName;

    @Column(name = "business_registration_number")
    private String businessRegistrationNumber;

    @Column(name = "representative_name")
    private String representativeName;

    private String description;

    @Column(name = "profile_path")
    private String profilePath;

    @Column(name = "contact_phone")
    private String contactPhone;

    private String account;

    @Column(name = "fee_rate")
    private BigDecimal feeRate;

    @Column(name = "foundation_hash")
    private String foundationHash;

    @Column(name = "account_status")
    private String accountStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "review_status")
    private ReviewStatus reviewStatus;

    @Column(name = "reject_reason")
    private String rejectReason;

    @Column(name = "campaign_wallet1")
    private String campaignWallet1;

    @Column(name = "campaign_wallet2")
    private String campaignWallet2;

    @Column(name = "campaign_wallet3")
    private String campaignWallet3;

    @Column(name = "bank_name")
    private String bankName;

//    @OneToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "wallet_no")
//    private Wallet wallet;
    //추후 연관관계 맺고 난 뒤 위에 원투원으로 변경할 예정
    @Column(name = "wallet_no")
    private Long walletNo;

    @Enumerated(EnumType.STRING)
    @Column(name = "foundation_type")
    private FoundationType foundationType;

}

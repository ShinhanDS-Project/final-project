package com.merge.final_project.org;

import com.merge.final_project.global.BaseEntity;
import com.merge.final_project.wallet.entity.Wallet;
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

    @Enumerated(EnumType.STRING)
    @Column(name = "account_status")
    private AccountStatus accountStatus;

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

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "wallet_no")
    private Wallet wallet;

    @Enumerated(EnumType.STRING)
    @Column(name = "foundation_type")
    private FoundationType foundationType;


    public void approved() {
        this.reviewStatus = ReviewStatus.APPROVED;
        this.accountStatus = AccountStatus.ACTIVE;
    }

    public void reject(String rejectReason) {
        this.reviewStatus = ReviewStatus.REJECTED;
        this.rejectReason = rejectReason;
    }

    public void updatePassword(String encodedPassword) {
        this.foundationPassword = encodedPassword;
    }

    public void update(String description, String contactPhone, String account, String bankName, BigDecimal feeRate) {
        this.description = description;
        this.contactPhone = contactPhone;
        this.account = account;
        this.bankName = bankName;
        this.feeRate = feeRate;
    }

    public void updateProfilePath(String profilePath) {
        this.profilePath = profilePath;
    }

    // 활동 보고서 작성 안 한 수혜자들이 있을 경우 (일단은) 기부단체가 비활성화됨. => 관리 이유.
    // 수혜 단체 쪽에 active 컬럼이 없었기에 이런 방식을 구현헀으나 추후 얘기 나눠볼 것.
    public void deactivate() {
        this.accountStatus = AccountStatus.INACTIVE;
    }

}

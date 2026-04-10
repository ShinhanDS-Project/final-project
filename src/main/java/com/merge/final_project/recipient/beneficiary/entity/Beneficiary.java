package com.merge.final_project.recipient.beneficiary.entity;

import com.merge.final_project.recipient.beneficiary.BeneficiaryType;
import com.merge.final_project.wallet.entity.Wallet;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "beneficiary")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class Beneficiary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long beneficiaryNo;

    @Column(name = "name")
    private String name;

    @Column(name = "email")
    private String email;

    @Column(name = "password")
    private String password;

    @Column(name = "phone")
    private String phone;

    @Column(name = "account")
    private String account;

    @Column(name = "beneficiary_hash")
    private String beneficiaryHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "beneficiary_type")
    private BeneficiaryType beneficiaryType;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "entry_code", nullable = false)
    private String entryCode;

    @Column(name = "key_no")
    private Long key_no;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "wallet_no", nullable = false)
    private Wallet wallet;

    /**
     * 정보 수정 메서드 (Dirty Checking 활용)
     */
    public void updateInfo(String name, String phone, String account, BeneficiaryType beneficiaryType) {
        this.name = name;
        this.phone = phone;
        this.account = account;
        this.beneficiaryType = beneficiaryType;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 비밀번호 수정 메서드
     */
    public void updatePassword(String encryptedPassword) {
        this.password = encryptedPassword;
        this.updatedAt = LocalDateTime.now();
    }
}

package com.merge.final_project.recipient.beneficiary;

import com.merge.final_project.wallet.Wallet;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "beneficiary")
@Getter
@NoArgsConstructor
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

    @Column(name = "privatekey_no", nullable = false)
    private Integer privatekeyNo;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "wallet_no", nullable = false)
    private Wallet wallet;
}

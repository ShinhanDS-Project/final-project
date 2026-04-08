package com.merge.final_project.db.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity(name = "DbBeneficiary")
@Table(name = "beneficiary")
@Getter
@Setter
@NoArgsConstructor
public class Beneficiary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "beneficiary_no")
    private Integer beneficiaryNo;

    @Column(name = "\"name\"", nullable = false)
    private String name;

    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "\"password\"", nullable = false)
    private String password;

    @Column(name = "entry_code", nullable = false)
    private Integer entryCode;

    @Column(name = "phone", nullable = false)
    private String phone;

    @Column(name = "account")
    private String account;

    @Column(name = "beneficiary_hash")
    private String beneficiaryHash;

    @Column(name = "beneficiary_type", nullable = false)
    private String beneficiaryType;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "wallet_no")
    private Integer walletNo;

    @Column(name = "key_no")
    private Integer keyNo;
}

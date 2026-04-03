package com.merge.final_project.recipient.beneficiary.entity;

import com.merge.final_project.recipient.beneficiary.BeneficiaryType;
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

    @Column(name = "entry_code")
    private int entry_code;

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


    @Column(name = "wallet_no")
    private Long wallet_no;

    @Column(name = "key_no")
    private Long key_no;
}

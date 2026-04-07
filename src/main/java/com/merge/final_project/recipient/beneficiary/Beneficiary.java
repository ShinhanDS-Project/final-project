package com.merge.final_project.recipient.beneficiary;

import com.merge.final_project.auth.useraccount.UsersAccount;
import com.merge.final_project.wallet.Wallet;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "beneficiary")
public class Beneficiary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "beneficiary_no")
    private Long beneficiaryNo;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String email;

    private String password;

    @Column(name = "entry_code")
    private Integer entryCode;

    private String phone;
    private String account;

    @Column(name = "beneficiary_hash")
    private String beneficiaryHash;

    @Column(name = "beneficiary_type")
    private String beneficiaryType;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "wallet_no", nullable = false)
    private Long walletNo;

    @Column(name = "key_no", nullable = false)
    private Long keyNo;

}

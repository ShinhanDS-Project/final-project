package com.merge.final_project.wallet;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "wallet")
@Getter
@NoArgsConstructor
public class Wallet {

    @Id
    @Column(name = "wallet_no", nullable = false)
    private String walletNo;

    @Enumerated(EnumType.STRING)
    @Column(name = "owner_type")
    private OwnerType ownerType;

    @Column(name = "owner_no")
    private String ownerNo;

    @Column(name = "wallet_address")
    private String walletAddress;

    @Column(name = "balance")
    private BigDecimal balance;

    @Column(name = "encrypted_private_key")
    private String encryptedPrivateKey;

    @Column(name = "wallet_hash")
    private String walletHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private WalletStatus status;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "last_used_at")
    private LocalDateTime lastUsedAt;
}

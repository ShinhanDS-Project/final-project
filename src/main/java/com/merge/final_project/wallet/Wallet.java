package com.merge.final_project.wallet;

import com.merge.final_project.wallet.entity.WalletStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "wallet")
public class Wallet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "wallet_no")
    private Long walletNo;

    @Column(name = "key_no", nullable = false)
    private Long keyNo;

    @Column(name = "wallet_type")
    private String walletType;

    @Column(name = "owner_no")
    private String ownerNo;

    @Column(name = "wallet_address", nullable = false, unique = true)
    private String walletAddress;

    private Integer balance;

    @Column(name = "wallet_hash")
    private String walletHash;

    @Enumerated(EnumType.STRING)
    private WalletStatus status;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "last_used_at")
    private LocalDateTime lastUsedAt;
}

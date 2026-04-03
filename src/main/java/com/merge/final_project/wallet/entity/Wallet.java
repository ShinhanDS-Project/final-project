package com.merge.final_project.wallet.entity;

import com.merge.final_project.blockchain.entity.Key;
import com.merge.final_project.campaign.campaigns.Campaign;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "wallet")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor
public class Wallet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "wallet_no")
    private Long walletNo;

    @ManyToOne
    @JoinColumn(name = "key_no")
    private Key key;

    @Enumerated(EnumType.STRING)
    @Column(name = "wallet_type", length = 50)
    private WalletType walletType;

    @Column(name = "owner_no")
    private Long ownerNo;

    @Column(name = "wallet_address", nullable = false, unique = true, length = 255)
    private String walletAddress;

    @Column(name = "balance", precision = 19, scale = 4)
    private BigDecimal balance;

    @Column(name = "wallet_hash", length = 255)
    private String walletHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 50)
    private WalletStatus status;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "last_used_at")
    private LocalDateTime lastUsedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "campaign_no")
    private Campaign campaign;

    public Wallet(
            WalletType walletType,
            Long ownerNo,
            String walletAddress,
            BigDecimal balance,
            String walletHash,
            WalletStatus status,
            Campaign campaign
    ) {
        this.walletType = walletType;
        this.ownerNo = ownerNo;
        this.walletAddress = walletAddress;
        this.balance = balance;
        this.walletHash = walletHash;
        this.status = status;
        this.campaign = campaign;
        this.createdAt = LocalDateTime.now();
    }

    public void updateLastUsedAt() {
        this.lastUsedAt = LocalDateTime.now();
    }

    public void changeStatus(WalletStatus status) {
        this.status = status;
    }

    public void updateBalance(BigDecimal balance) {
        this.balance = balance;
    }
}

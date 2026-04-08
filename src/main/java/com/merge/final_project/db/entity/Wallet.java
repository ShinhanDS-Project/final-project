package com.merge.final_project.db.entity;

import com.merge.final_project.db.entity.id.WalletId;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity(name = "DbWallet")
@Table(name = "wallet")
@Getter
@Setter
@NoArgsConstructor
public class Wallet {

    @EmbeddedId
    private WalletId id;

    @Column(name = "wallet_type")
    private String walletType;

    @Column(name = "owner_no")
    private String ownerNo;

    @Column(name = "wallet_address", nullable = false)
    private String walletAddress;

    @Column(name = "balance")
    private Integer balance;

    @Column(name = "wallet_hash")
    private String walletHash;

    @Column(name = "status")
    private String status;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "last_used_at")
    private LocalDateTime lastUsedAt;

    @Column(name = "campaign_no")
    private Integer campaignNo;
}

package com.merge.final_project.wallet.entity;

import com.merge.final_project.blockchain.entity.Key;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "wallet")
@Getter
@Setter
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

    // 지갑 생성 (기본 정보 설정 및 생성 시간 기록)
    public Wallet(
            WalletType walletType,
            Long ownerNo,
            String walletAddress,
            BigDecimal balance,
            String walletHash,
            WalletStatus status
    ) {
        this.walletType = walletType;
        this.ownerNo = ownerNo;
        this.walletAddress = walletAddress;
        this.balance = balance;
        this.walletHash = walletHash;
        this.status = status;
        this.createdAt = LocalDateTime.now();
    }

    // 지갑 마지막 사용 시간 갱신
    public void updateLastUsedAt() {
        this.lastUsedAt = LocalDateTime.now();
    }

    // 지갑 상태 변경(ACTIVE, INACTIVE 등)
    public void changeStatus(WalletStatus status) {
        this.status = status;
    }

    // 지갑 잔액 업데이트 (온체인 결과 반영)
    public void updateBalance(BigDecimal balance) {
        this.balance = balance;
    }
}

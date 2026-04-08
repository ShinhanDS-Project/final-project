package com.merge.final_project.wallet.repository;

import com.merge.final_project.wallet.entity.Wallet;
import com.merge.final_project.wallet.entity.WalletType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WalletLookupRepository extends JpaRepository<Wallet, Long> {

    Optional<Wallet> findByWalletAddressIgnoreCase(String walletAddress);

    Optional<Wallet> findFirstByWalletType(WalletType walletType);

    Optional<Wallet> findByWalletTypeAndOwnerNo(WalletType walletType, Long ownerNo);

    boolean existsByWalletTypeAndOwnerNo(WalletType walletType, Long ownerNo);
}

package com.merge.final_project.db.repository;

import com.merge.final_project.db.entity.Wallet;
import com.merge.final_project.db.entity.id.WalletId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WalletRepository extends JpaRepository<Wallet, WalletId> {
    Optional<Wallet> findByWalletTypeAndOwnerNo(String walletType, String ownerNo);

    List<Wallet> findAllByWalletTypeAndOwnerNo(String walletType, String ownerNo);

    boolean existsByWalletTypeAndOwnerNo(String walletType, String ownerNo);

    Optional<Wallet> findByWalletAddress(String walletAddress);

    Optional<Wallet> findByWalletAddressIgnoreCase(String walletAddress);

    Optional<Wallet> findByIdWalletNo(Long walletNo);
}

package com.merge.final_project.wallet.repository;

import com.merge.final_project.wallet.entity.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WalletRepository extends JpaRepository<Wallet, Long> {

    Optional<Wallet> findByWalletNo(Long walletNo);

    Optional<Wallet> findByWalletAddress(String walletAddress);
}

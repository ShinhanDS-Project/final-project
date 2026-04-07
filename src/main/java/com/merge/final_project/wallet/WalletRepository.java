package com.merge.final_project.wallet;

import com.merge.final_project.wallet.entity.WalletStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WalletRepository extends JpaRepository<Wallet, Long> {
    Optional<Wallet> findFirstByWalletAddressInAndStatus(List<String> walletAddresses, WalletStatus status);
}

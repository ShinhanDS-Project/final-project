package com.merge.final_project.wallet.repository;

import com.merge.final_project.wallet.entity.Wallet;
import com.merge.final_project.wallet.entity.WalletStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WalletRepository extends JpaRepository<Wallet, Long> {

    // walletNo(논리 PK)로 지갑 조회
//    Optional<Wallet> findByWalletNo(Long walletNo);

    // 여러 주소 중 상태가 일치하는 첫 번째 지갑 조회
    Optional<Wallet> findFirstByWalletAddressInAndStatus(List<String> walletAddresses, WalletStatus status);

    // 지갑 주소로 단일 지갑 조회
    Optional<Wallet> findByWalletAddress(String walletAddress);
}

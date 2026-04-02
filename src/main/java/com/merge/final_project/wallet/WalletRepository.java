package com.merge.final_project.wallet;

import com.merge.final_project.auth.role.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WalletRepository extends JpaRepository<Wallet, Long> {
    //지갑 상태 확인 (for 캠페인 등록)
    Optional<Wallet> findFirstByWalletAddressInAndStatus(List<String> addresses, String status);
}

package com.merge.final_project.wallet.repository;

import com.merge.final_project.wallet.entity.Wallet;
import com.merge.final_project.wallet.entity.WalletType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WalletLookupRepository extends JpaRepository<Wallet, Long> {

    /**
     * 지갑 주소(대소문자 무시)로 단건 조회.
     */
    Optional<Wallet> findByWalletAddressIgnoreCase(String walletAddress);

    /**
     * 특정 타입(HOT/COLD 등)의 첫 번째 지갑 조회.
     * 서버 지갑 singleton 조회에 사용한다.
     */
    Optional<Wallet> findFirstByWalletType(WalletType walletType);

    /**
     * wallet_type + owner_no 조합으로 단건 조회.
     * 도메인 소유자(유저/단체/수혜자) 지갑 조회의 표준 메서드다.
     */
    Optional<Wallet> findByWalletTypeAndOwnerNo(WalletType walletType, Long ownerNo);

    /**
     * wallet_type + owner_no 조합 존재 여부 확인.
     * 중복 지갑 생성 방지용 사전 체크에 사용한다.
     */
    boolean existsByWalletTypeAndOwnerNo(WalletType walletType, Long ownerNo);
}

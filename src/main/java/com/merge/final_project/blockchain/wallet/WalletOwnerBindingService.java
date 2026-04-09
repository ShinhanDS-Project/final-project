package com.merge.final_project.blockchain.wallet;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class WalletOwnerBindingService {

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * 기부자 지갑 생성 후 users 테이블의 wallet_no를 연결한다.
     */
    @Transactional
    public void bindUserWallet(Long userNo, Long walletNo) {
        entityManager.createNativeQuery("UPDATE users SET wallet_no = :walletNo WHERE user_no = :userNo")
                .setParameter("walletNo", walletNo)
                .setParameter("userNo", userNo)
                .executeUpdate();
    }

    /**
     * 기부단체 지갑 번호를 foundation 테이블에 연결한다.
     */
    @Transactional
    public void bindFoundationWallet(Long foundationNo, Long walletNo) {
        entityManager.createNativeQuery("UPDATE foundation SET wallet_no = :walletNo WHERE foundation_no = :foundationNo")
                .setParameter("walletNo", walletNo)
                .setParameter("foundationNo", foundationNo)
                .executeUpdate();
    }

    /**
     * 사전 생성한 캠페인 지갑 주소 3개를 foundation 행에 저장한다.
     * 이후 캠페인 생성 시 이 주소들을 재사용한다.
     */
    @Transactional
    public void bindFoundationCampaignWallets(Long foundationNo, String wallet1, String wallet2, String wallet3) {
        entityManager.createNativeQuery(
                        "UPDATE foundation " +
                                "SET campaign_wallet1 = :wallet1, campaign_wallet2 = :wallet2, campaign_wallet3 = :wallet3 " +
                                "WHERE foundation_no = :foundationNo"
                )
                .setParameter("wallet1", wallet1)
                .setParameter("wallet2", wallet2)
                .setParameter("wallet3", wallet3)
                .setParameter("foundationNo", foundationNo)
                .executeUpdate();
    }

    /**
     * beneficiary 도메인에서 쓰는 wallet/key 참조를 beneficiary 행에 저장한다.
     */
    @Transactional
    public void bindBeneficiaryWallet(Long beneficiaryNo, Long walletNo, Long keyNo) {
        entityManager.createNativeQuery(
                        "UPDATE beneficiary " +
                                "SET wallet_no = :walletNo, key_no = :keyNo, updated_at = NOW() " +
                                "WHERE beneficiary_no = :beneficiaryNo"
                )
                .setParameter("walletNo", walletNo)
                .setParameter("keyNo", keyNo)
                .setParameter("beneficiaryNo", beneficiaryNo)
                .executeUpdate();
    }
}

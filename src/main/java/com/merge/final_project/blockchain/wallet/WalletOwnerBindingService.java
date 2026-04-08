package com.merge.final_project.blockchain.wallet;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class WalletOwnerBindingService {

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional
    public void bindUserWallet(Long userNo, Long walletNo) {
        entityManager.createNativeQuery("UPDATE users SET wallet_no = :walletNo WHERE user_no = :userNo")
                .setParameter("walletNo", walletNo)
                .setParameter("userNo", userNo)
                .executeUpdate();
    }

    @Transactional
    public void bindFoundationWallet(Long foundationNo, Long walletNo) {
        entityManager.createNativeQuery("UPDATE foundation SET wallet_no = :walletNo WHERE foundation_no = :foundationNo")
                .setParameter("walletNo", walletNo)
                .setParameter("foundationNo", foundationNo)
                .executeUpdate();
    }

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

package com.merge.final_project.auth.useraccount;

import com.merge.final_project.blockchain.wallet.WalletProvisionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SignupWalletHookService {

    private final WalletProvisionService walletProvisionService;

    @Transactional
    public void onUserSignupCompleted(Long userNo) {
        walletProvisionService.createUserWalletAfterSignup(userNo);
    }

    @Transactional
    public void onFoundationSignupCompleted(Long foundationNo) {
        walletProvisionService.createFoundationWalletAfterSignup(foundationNo);
    }

    @Transactional
    public void onBeneficiarySignupCompleted(Long beneficiaryNo) {
        walletProvisionService.createBeneficiaryWalletAfterSignup(beneficiaryNo);
    }
}

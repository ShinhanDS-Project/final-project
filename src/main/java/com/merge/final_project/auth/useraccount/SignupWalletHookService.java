package com.merge.final_project.auth.useraccount;

import com.merge.final_project.blockchain.wallet.WalletProvisionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SignupWalletHookService {
    /**
     * 회원가입 트랜잭션 마지막에서 호출하는 훅 계층.
     * 회원가입 도메인 로직과 지갑 생성 로직을 분리하기 위한 목적이다.
     */

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

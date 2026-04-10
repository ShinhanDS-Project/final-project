package com.merge.final_project.auth.useraccount;

import com.merge.final_project.blockchain.wallet.WalletProvisionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SignupWalletHookService {

    private final WalletProvisionService walletProvisionService;

    /**
     * 기부자 회원가입 완료 시 호출되는 훅.
     * 계정 생성 로직과 지갑 생성 로직을 분리해, 가입 도메인 코드 변경 없이
     * 지갑 프로비저닝 정책을 독립적으로 확장할 수 있도록 한다.
     */
    @Transactional
    public void onUserSignupCompleted(Long userNo) {
        walletProvisionService.createUserWalletAfterSignup(userNo);
    }

    /**
     * 기부단체 승인 완료 시 호출되는 훅.
     * 단체 지갑 생성과 함께 캠페인용 지갑 슬롯 준비까지 프로비저닝 서비스에서 처리한다.
     */
    @Transactional
    public void onFoundationSignupCompleted(Long foundationNo) {
        walletProvisionService.createFoundationWalletAfterSignup(foundationNo);
    }

    /**
     * 수혜자 회원가입 완료 시 호출되는 훅.
     * 수혜자 지갑 생성 후 beneficiary 테이블에 wallet/key 참조를 바인딩한다.
     */
    @Transactional
    public void onBeneficiarySignupCompleted(Long beneficiaryNo) {
        walletProvisionService.createBeneficiaryWalletAfterSignup(beneficiaryNo);
    }
}

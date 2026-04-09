package com.merge.final_project.blockchain.wallet;

import com.merge.final_project.blockchain.entity.Key;
import com.merge.final_project.blockchain.gas.GasStationService;
import com.merge.final_project.blockchain.repository.KeyRepository;
import com.merge.final_project.blockchain.security.WalletCredentials;
import com.merge.final_project.blockchain.security.WalletCryptoService;
import com.merge.final_project.org.FoundationRepository;
import com.merge.final_project.recipient.beneficiary.repository.BeneficiaryRepository;
import com.merge.final_project.user.users.UserRepository;
import com.merge.final_project.wallet.ServerWalletType;
import com.merge.final_project.wallet.entity.Wallet;
import com.merge.final_project.wallet.entity.WalletStatus;
import com.merge.final_project.wallet.entity.WalletType;
import com.merge.final_project.wallet.repository.WalletLookupRepository;
import com.merge.final_project.wallet.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class WalletProvisionService {

    private final WalletRepository walletRepository;
    private final WalletLookupRepository walletLookupRepository;
    private final UserRepository userRepository;
    private final FoundationRepository foundationRepository;
    private final BeneficiaryRepository beneficiaryRepository;
    private final KeyRepository keyRepository;
    private final WalletCryptoService walletCryptoService;
    private final GasStationService gasStationService;
    private final WalletOwnerBindingService walletOwnerBindingService;

    /**
     * 공통 기본값으로 지갑/키를 생성한다.
     * ACTIVE 지갑이면 즉시 사용 가능하도록 초기 POL 가스비 충전을 시도한다.
     */
    @Transactional
    public Wallet createBaseWallet(WalletType walletType, Long ownerNo, WalletStatus status) {
        WalletCredentials credentials = walletCryptoService.createWalletCredentials();
        Key savedKey = keyRepository.save(
                Key.builder()
                        .privateKey(credentials.encryptedPrivateKey())
                        .build()
        );

        Wallet wallet = Wallet.builder()
                .key(savedKey)
                .walletType(walletType)
                .ownerNo(ownerNo)
                .walletAddress(credentials.address())
                .balance(BigDecimal.ZERO)
                .walletHash(null)
                .status(status)
                .createdAt(LocalDateTime.now())
                .lastUsedAt(LocalDateTime.now())
                .build();

        Wallet savedWallet = walletRepository.save(wallet);
        if (status == WalletStatus.ACTIVE) {
            gasStationService.fundInitialPol(savedWallet);
        }
        return savedWallet;
    }

    /**
     * 기부자 가입 완료 후 지갑을 생성하고 users.wallet_no를 바인딩한다.
     */
    @Transactional
    public Wallet createUserWalletAfterSignup(Long userNo) {
        userRepository.findById(userNo)
                .orElseThrow(() -> new IllegalArgumentException("user not found: " + userNo));

        if (walletLookupRepository.existsByWalletTypeAndOwnerNo(WalletType.USER, userNo)) {
            throw new IllegalStateException("user wallet already exists: " + userNo);
        }

        Wallet wallet = createBaseWallet(WalletType.USER, userNo, WalletStatus.ACTIVE);
        walletOwnerBindingService.bindUserWallet(userNo, wallet.getWalletNo());
        return wallet;
    }

    /**
     * 기부단체 승인 완료 후 단체 지갑 1개를 만들고,
     * 추후 캠페인 생성에 사용할 캠페인 지갑 주소 3개를 미리 준비한다.
     */
    @Transactional
    public Wallet createFoundationWalletAfterSignup(Long foundationNo) {
        foundationRepository.findById(foundationNo)
                .orElseThrow(() -> new IllegalArgumentException("foundation not found: " + foundationNo));

        if (walletLookupRepository.existsByWalletTypeAndOwnerNo(WalletType.FOUNDATION, foundationNo)) {
            throw new IllegalStateException("foundation wallet already exists: " + foundationNo);
        }

        Wallet wallet = createBaseWallet(WalletType.FOUNDATION, foundationNo, WalletStatus.ACTIVE);
        walletOwnerBindingService.bindFoundationWallet(foundationNo, wallet.getWalletNo());

        List<Wallet> campaignWallets = createInitialCampaignWalletSetForFoundation(foundationNo);
        walletOwnerBindingService.bindFoundationCampaignWallets(
                foundationNo,
                campaignWallets.get(0).getWalletAddress(),
                campaignWallets.get(1).getWalletAddress(),
                campaignWallets.get(2).getWalletAddress()
        );
        return wallet;
    }

    /**
     * 수혜자 가입 완료 후 지갑을 생성하고 beneficiary.wallet_no/key_no를 저장한다.
     */
    @Transactional
    public Wallet createBeneficiaryWalletAfterSignup(Long beneficiaryNo) {
        beneficiaryRepository.findById(beneficiaryNo)
                .orElseThrow(() -> new IllegalArgumentException("beneficiary not found: " + beneficiaryNo));

        if (walletLookupRepository.existsByWalletTypeAndOwnerNo(WalletType.BENEFICIARY, beneficiaryNo)) {
            throw new IllegalStateException("beneficiary wallet already exists: " + beneficiaryNo);
        }

        Wallet wallet = createBaseWallet(WalletType.BENEFICIARY, beneficiaryNo, WalletStatus.ACTIVE);
        Long keyNo = wallet.getKey() == null ? null : wallet.getKey().getKeyNo();
        walletOwnerBindingService.bindBeneficiaryWallet(beneficiaryNo, wallet.getWalletNo(), keyNo);
        return wallet;
    }

    /**
     * HOT/COLD 서버 지갑이 없으면 생성하고, 있으면 기존 지갑을 재사용한다.
     */
    @Transactional
    public Wallet createServerWallet(ServerWalletType serverWalletType) {
        WalletType walletType = serverWalletType == ServerWalletType.HOT ? WalletType.HOT : WalletType.COLD;
        return walletLookupRepository.findFirstByWalletType(walletType)
                .orElseGet(() -> createBaseWallet(walletType, defaultServerOwnerNo(serverWalletType), WalletStatus.ACTIVE));
    }

    /**
     * 단체 ownerNo에 연결된 비활성 캠페인 지갑 3개를 생성한다.
     */
    private List<Wallet> createInitialCampaignWalletSetForFoundation(Long foundationNo) {
        List<Wallet> campaignWallets = new ArrayList<>(3);
        for (int idx = 1; idx <= 3; idx++) {
            Long ownerNo = foundationNo;
            campaignWallets.add(createBaseWallet(WalletType.CAMPAIGN, ownerNo, WalletStatus.INACTIVE));
        }
        return campaignWallets;
    }

    /**
     * 서버 지갑 ownerNo는 실제 도메인 PK와 충돌하지 않도록 음수 상수를 사용한다.
     */
    private Long defaultServerOwnerNo(ServerWalletType serverWalletType) {
        return serverWalletType == ServerWalletType.HOT ? -1L : -2L;
    }
}

package com.merge.final_project.blockchain.wallet;

import com.merge.final_project.blockchain.entity.Key;
import com.merge.final_project.blockchain.gas.GasStationService;
import com.merge.final_project.blockchain.repository.KeyRepository;
import com.merge.final_project.blockchain.security.WalletCredentials;
import com.merge.final_project.blockchain.security.WalletCryptoService;
import com.merge.final_project.org.FoundationRepository;
import com.merge.final_project.recipient.beneficiary.repository.BeneficiaryRepository;
import com.merge.final_project.user.users.UserRepository;
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

    private List<Wallet> createInitialCampaignWalletSetForFoundation(Long foundationNo) {
        List<Wallet> campaignWallets = new ArrayList<>(3);
        for (int idx = 1; idx <= 3; idx++) {
            campaignWallets.add(createBaseWallet(WalletType.CAMPAIGN, foundationNo, WalletStatus.INACTIVE));
        }
        return campaignWallets;
    }
}

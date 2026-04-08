package com.merge.final_project.blockchain.wallet;

import com.merge.final_project.blockchain.gas.GasStationService;
import com.merge.final_project.blockchain.security.WalletCredentials;
import com.merge.final_project.blockchain.security.WalletCryptoService;
import com.merge.final_project.db.entity.Beneficiary;
import com.merge.final_project.db.entity.Foundation;
import com.merge.final_project.db.entity.KeyEntity;
import com.merge.final_project.db.entity.UserEntity;
import com.merge.final_project.db.entity.Wallet;
import com.merge.final_project.db.entity.id.WalletId;
import com.merge.final_project.db.repository.BeneficiaryRepository;
import com.merge.final_project.db.repository.FoundationRepository;
import com.merge.final_project.db.repository.KeyEntityRepository;
import com.merge.final_project.db.repository.UserEntityRepository;
import com.merge.final_project.db.repository.WalletRepository;
import com.merge.final_project.wallet.ServerWalletType;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class WalletProvisionService {
    /**
     * 지갑 생성 규칙은 공통이고, 타입별 생성 트리거만 다르다.
     * 운영 스키마(wallet + key)를 기준으로 지갑/키를 함께 생성한다.
     */

    private static final String WALLET_TYPE_SERVER = "SERVER";
    private static final String WALLET_TYPE_USER = "USER";
    private static final String WALLET_TYPE_FOUNDATION = "FOUNDATION";
    private static final String WALLET_TYPE_BENEFICIARY = "BENEFICIARY";
    private static final String WALLET_TYPE_CAMPAIGN = "CAMPAIGN";

    private final WalletRepository walletRepository;
    private final UserEntityRepository userEntityRepository;
    private final FoundationRepository foundationRepository;
    private final BeneficiaryRepository beneficiaryRepository;
    private final KeyEntityRepository keyEntityRepository;
    private final WalletCryptoService walletCryptoService;
    private final GasStationService gasStationService;
    private final JdbcTemplate jdbcTemplate;

    @Transactional
    public Wallet createBaseWallet(String walletType, String ownerNo) {
        WalletCredentials credentials = walletCryptoService.createWalletCredentials();
        KeyEntity keyEntity = new KeyEntity();
        keyEntity.setPrivateKey(credentials.encryptedPrivateKey());
        KeyEntity savedKey = keyEntityRepository.save(keyEntity);

        LocalDateTime now = LocalDateTime.now();
        Long walletNo = jdbcTemplate.queryForObject(
                """
                INSERT INTO wallet (
                    key_no, wallet_type, owner_no, wallet_address, balance, status, created_at, last_used_at
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                RETURNING wallet_no
                """,
                Long.class,
                savedKey.getKeyNo(),
                walletType,
                ownerNo,
                credentials.address(),
                0,
                "ACTIVE",
                now,
                now
        );

        if (walletNo == null) {
            throw new IllegalStateException("wallet insert failed");
        }

        WalletId walletId = new WalletId();
        walletId.setWalletNo(walletNo);
        walletId.setKeyNo(savedKey.getKeyNo());
        Wallet savedWallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new IllegalStateException("wallet row not found after insert. walletNo=" + walletNo));

        gasStationService.fundInitialPol(savedWallet);
        return savedWallet;
    }

    /**
     * 사용자 회원가입 완료 후 호출.
     */
    @Transactional
    public Wallet createUserWalletAfterSignup(Long userNo) {
        Integer userNoInt = toInt(userNo, "userNo");
        UserEntity user = userEntityRepository.findById(userNoInt)
                .orElseThrow(() -> new IllegalArgumentException("user not found: " + userNo));

        if (walletRepository.existsByWalletTypeAndOwnerNo(WALLET_TYPE_USER, userNo.toString())) {
            throw new IllegalStateException("user wallet already exists: " + userNo);
        }

        Wallet wallet = createBaseWallet(WALLET_TYPE_USER, userNo.toString());
        if (wallet.getId() == null) {
            throw new IllegalStateException("wallet id is missing");
        }
        user.setWalletNo(toInt(wallet.getId().getWalletNo(), "walletNo"));
        user.setPrivatekeyNo(toInt(wallet.getId().getKeyNo(), "keyNo"));
        userEntityRepository.save(user);
        return wallet;
    }

    /**
     * 기부단체 회원가입 완료 후 호출.
     * 단체 기본 지갑 + 캠페인 재사용 지갑 3개를 즉시 만든다.
     */
    @Transactional
    public Wallet createFoundationWalletAfterSignup(Long foundationNo) {
        Integer foundationNoInt = toInt(foundationNo, "foundationNo");
        Foundation foundation = foundationRepository.findById(foundationNoInt)
                .orElseThrow(() -> new IllegalArgumentException("foundation not found: " + foundationNo));

        if (walletRepository.existsByWalletTypeAndOwnerNo(WALLET_TYPE_FOUNDATION, foundationNo.toString())) {
            throw new IllegalStateException("foundation wallet already exists: " + foundationNo);
        }

        Wallet wallet = createBaseWallet(WALLET_TYPE_FOUNDATION, foundationNo.toString());
        if (wallet.getId() == null) {
            throw new IllegalStateException("wallet id is missing");
        }
        foundation.setWalletNo(toInt(wallet.getId().getWalletNo(), "walletNo"));

        List<Wallet> campaignWallets = createInitialCampaignWalletSetForFoundation(foundationNo);
        foundation.setCampaignWallet1(String.valueOf(campaignWallets.get(0).getId().getWalletNo()));
        foundation.setCampaignWallet2(String.valueOf(campaignWallets.get(1).getId().getWalletNo()));
        foundation.setCampaignWallet3(String.valueOf(campaignWallets.get(2).getId().getWalletNo()));
        foundationRepository.save(foundation);
        return wallet;
    }

    /**
     * 수혜자 회원가입 완료 후 호출.
     */
    @Transactional
    public Wallet createBeneficiaryWalletAfterSignup(Long beneficiaryNo) {
        Integer beneficiaryNoInt = toInt(beneficiaryNo, "beneficiaryNo");
        Beneficiary beneficiary = beneficiaryRepository.findById(beneficiaryNoInt)
                .orElseThrow(() -> new IllegalArgumentException("beneficiary not found: " + beneficiaryNo));

        if (walletRepository.existsByWalletTypeAndOwnerNo(WALLET_TYPE_BENEFICIARY, beneficiaryNo.toString())) {
            throw new IllegalStateException("beneficiary wallet already exists: " + beneficiaryNo);
        }

        Wallet wallet = createBaseWallet(WALLET_TYPE_BENEFICIARY, beneficiaryNo.toString());
        if (wallet.getId() == null) {
            throw new IllegalStateException("wallet id is missing");
        }
        beneficiary.setWalletNo(toInt(wallet.getId().getWalletNo(), "walletNo"));
        beneficiary.setKeyNo(toInt(wallet.getId().getKeyNo(), "keyNo"));
        beneficiaryRepository.save(beneficiary);
        return wallet;
    }

    /**
     * 단체 단위 캠페인 지갑 3개를 고정 생성한다.
     */
    private List<Wallet> createInitialCampaignWalletSetForFoundation(Long foundationNo) {
        List<Wallet> campaignWallets = new ArrayList<>(3);
        for (int idx = 1; idx <= 3; idx++) {
            String ownerNo = foundationNo + ":CAMPAIGN:" + idx;
            if (walletRepository.existsByWalletTypeAndOwnerNo(WALLET_TYPE_CAMPAIGN, ownerNo)) {
                throw new IllegalStateException("campaign wallet already exists for foundation: " + ownerNo);
            }
            campaignWallets.add(createBaseWallet(WALLET_TYPE_CAMPAIGN, ownerNo));
        }
        return campaignWallets;
    }

    /**
     * 서버 지갑(HOT/COLD) 생성 또는 기존 지갑 재사용.
     */
    @Transactional
    public Wallet createServerWallet(ServerWalletType serverWalletType) {
        String ownerNo = serverWalletType.name();
        return walletRepository.findByWalletTypeAndOwnerNo(WALLET_TYPE_SERVER, ownerNo)
                .orElseGet(() -> createBaseWallet(WALLET_TYPE_SERVER, ownerNo));
    }

    private Integer toInt(Long value, String fieldName) {
        if (value == null) {
            throw new IllegalArgumentException(fieldName + " is required");
        }
        if (value > Integer.MAX_VALUE || value < Integer.MIN_VALUE) {
            throw new IllegalArgumentException(fieldName + " out of int range: " + value);
        }
        return value.intValue();
    }
}

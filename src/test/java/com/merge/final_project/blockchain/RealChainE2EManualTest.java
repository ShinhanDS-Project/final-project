package com.merge.final_project.blockchain;

import com.merge.final_project.blockchain.dto.BlockchainTransferResponse;
import com.merge.final_project.blockchain.entity.Key;
import com.merge.final_project.blockchain.entity.Transaction;
import com.merge.final_project.blockchain.entity.TransactionEventType;
import com.merge.final_project.blockchain.entity.TransactionStatus;
import com.merge.final_project.blockchain.repository.KeyRepository;
import com.merge.final_project.blockchain.repository.TransactionRepository;
import com.merge.final_project.blockchain.security.WalletCredentials;
import com.merge.final_project.blockchain.security.WalletCryptoService;
import com.merge.final_project.blockchain.service.BlockchainService;
import com.merge.final_project.blockchain.service.BlockchainTransferService;
import com.merge.final_project.blockchain.service.SettlementTransactionService;
import com.merge.final_project.blockchain.service.TokenAmountConverter;
import com.merge.final_project.blockchain.tx.BlockchainTransferClient;
import com.merge.final_project.blockchain.tx.TransferResult;
import com.merge.final_project.blockchain.wallet.WalletOwnerBindingService;
import com.merge.final_project.campaign.campaigns.ApprovalStatus;
import com.merge.final_project.campaign.campaigns.CampaignCategory;
import com.merge.final_project.campaign.campaigns.CampaignStatus;
import com.merge.final_project.campaign.campaigns.entity.Campaign;
import com.merge.final_project.campaign.campaigns.repository.CampaignRepository;
import com.merge.final_project.org.AccountStatus;
import com.merge.final_project.org.Foundation;
import com.merge.final_project.org.FoundationRepository;
import com.merge.final_project.org.FoundationType;
import com.merge.final_project.org.ReviewStatus;
import com.merge.final_project.recipient.beneficiary.BeneficiaryType;
import com.merge.final_project.recipient.beneficiary.entity.Beneficiary;
import com.merge.final_project.recipient.beneficiary.repository.BeneficiaryRepository;
import com.merge.final_project.redemption.RedemptionStatus;
import com.merge.final_project.redemption.RequesterType;
import com.merge.final_project.redemption.dto.request.RedemptionRequest;
import com.merge.final_project.redemption.dto.response.RedemptionResponse;
import com.merge.final_project.redemption.entity.Redemption;
import com.merge.final_project.redemption.repository.RedemptionRepository;
import com.merge.final_project.redemption.service.RedemptionService;
import com.merge.final_project.user.signUp.UserSignUpRepository;
import com.merge.final_project.user.users.LoginType;
import com.merge.final_project.user.users.User;
import com.merge.final_project.user.users.UserStatus;
import com.merge.final_project.wallet.entity.Wallet;
import com.merge.final_project.wallet.entity.WalletStatus;
import com.merge.final_project.wallet.entity.WalletType;
import com.merge.final_project.wallet.repository.WalletLookupRepository;
import com.merge.final_project.wallet.repository.WalletRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.util.ReflectionTestUtils;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class RealChainE2EManualTest {

    private static final BigInteger USER_GAS_TOPUP_WEI = new BigInteger("10000000000000000");
    private static final BigInteger CONTRACT_GAS_TOPUP_WEI = new BigInteger("30000000000000000");
    private static final BigInteger OWNER_GAS_RESERVE_WEI = new BigInteger("20000000000000000");
    private static final BigInteger TX_HEADROOM_WEI = new BigInteger("5000000000000000");
    private static final BigInteger GAS_BUFFER_NUMERATOR = BigInteger.valueOf(125);
    private static final BigInteger GAS_BUFFER_DENOMINATOR = BigInteger.valueOf(100);
    private static final BigInteger NATIVE_TRANSFER_GAS_LIMIT = BigInteger.valueOf(21_000L);
    private static final Pattern EVM_PRIVATE_KEY = Pattern.compile("^(0x)?[a-fA-F0-9]{64}$");

    private final Web3j web3j;
    private final UserSignUpRepository userSignUpRepository;
    private final BeneficiaryRepository beneficiaryRepository;
    private final FoundationRepository foundationRepository;
    private final CampaignRepository campaignRepository;
    private final WalletRepository walletRepository;
    private final WalletLookupRepository walletLookupRepository;
    private final KeyRepository keyRepository;
    private final WalletOwnerBindingService walletOwnerBindingService;
    private final WalletCryptoService walletCryptoService;
    private final BlockchainTransferClient blockchainTransferClient;
    private final BlockchainTransferService blockchainTransferService;
    private final SettlementTransactionService settlementTransactionService;
    private final RedemptionService redemptionService;
    private final RedemptionRepository redemptionRepository;
    private final TransactionRepository transactionRepository;
    private final BlockchainService blockchainService;
    private final TokenAmountConverter tokenAmountConverter;

    @Value("${blockchain.contract.owner-address:}")
    private String contractOwnerAddress;

    @Value("${blockchain.tx.gas-limit:300000}")
    private long transferTxGasLimit;

    @Value("${blockchain.gas.limit:600000}")
    private long contractTxGasLimit;

    @Autowired
    RealChainE2EManualTest(
            Web3j web3j,
            UserSignUpRepository userSignUpRepository,
            BeneficiaryRepository beneficiaryRepository,
            FoundationRepository foundationRepository,
            CampaignRepository campaignRepository,
            WalletRepository walletRepository,
            WalletLookupRepository walletLookupRepository,
            KeyRepository keyRepository,
            WalletOwnerBindingService walletOwnerBindingService,
            WalletCryptoService walletCryptoService,
            BlockchainTransferClient blockchainTransferClient,
            BlockchainTransferService blockchainTransferService,
            SettlementTransactionService settlementTransactionService,
            RedemptionService redemptionService,
            RedemptionRepository redemptionRepository,
            TransactionRepository transactionRepository,
            BlockchainService blockchainService,
            TokenAmountConverter tokenAmountConverter
    ) {
        this.web3j = web3j;
        this.userSignUpRepository = userSignUpRepository;
        this.beneficiaryRepository = beneficiaryRepository;
        this.foundationRepository = foundationRepository;
        this.campaignRepository = campaignRepository;
        this.walletRepository = walletRepository;
        this.walletLookupRepository = walletLookupRepository;
        this.keyRepository = keyRepository;
        this.walletOwnerBindingService = walletOwnerBindingService;
        this.walletCryptoService = walletCryptoService;
        this.blockchainTransferClient = blockchainTransferClient;
        this.blockchainTransferService = blockchainTransferService;
        this.settlementTransactionService = settlementTransactionService;
        this.redemptionService = redemptionService;
        this.redemptionRepository = redemptionRepository;
        this.transactionRepository = transactionRepository;
        this.blockchainService = blockchainService;
        this.tokenAmountConverter = tokenAmountConverter;
    }

    @Test
    void runRealChainFlow() throws Exception {
        String runId = "manual-" + System.currentTimeMillis();

        Wallet ownerWallet = walletLookupRepository.findByWalletAddressIgnoreCase(contractOwnerAddress)
                .orElseThrow(() -> new IllegalStateException("contract owner wallet not found in DB: " + contractOwnerAddress));
        Wallet hotWallet = walletLookupRepository.findFirstByWalletType(WalletType.HOT)
                .orElseThrow(() -> new IllegalStateException("HOT wallet not found in DB"));
        String ownerPrivateKey = resolvePrivateKey(ownerWallet);

        User user = userSignUpRepository.save(
                User.builder()
                        .email(runId + "@user.test")
                        .passwordHash("manual-password")
                        .name("user-" + runId)
                        .nameHash("nh-" + UUID.randomUUID())
                        .phone(uniquePhone())
                        .birth(LocalDate.of(1990, 1, 1))
                        .status(UserStatus.ACTIVE)
                        .loginType(LoginType.LOCAL)
                        .loginCount(0)
                        .build()
        );
        Wallet userWallet = createWallet(WalletType.USER, user.getUserNo(), WalletStatus.ACTIVE);
        walletOwnerBindingService.bindUserWallet(user.getUserNo(), userWallet.getWalletNo());

        Beneficiary beneficiary = beneficiaryRepository.save(
                Beneficiary.builder()
                        .name("beneficiary-" + runId)
                        .email(runId + "@beneficiary.test")
                        .password("beneficiary-password")
                        .phone(uniquePhone())
                        .account("3333-12-123456")
                        .beneficiaryHash("init_hash")
                        .beneficiaryType(BeneficiaryType.INDIVIDUAL)
                        .entryCode(UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .build()
        );
        Wallet beneficiaryWallet = createWallet(WalletType.BENEFICIARY, beneficiary.getBeneficiaryNo(), WalletStatus.ACTIVE);
        walletOwnerBindingService.bindBeneficiaryWallet(
                beneficiary.getBeneficiaryNo(),
                beneficiaryWallet.getWalletNo(),
                beneficiaryWallet.getKey().getKeyNo()
        );

        Foundation foundation = foundationRepository.save(
                Foundation.builder()
                        .foundationEmail(runId + "@foundation.test")
                        .foundationName("foundation-" + runId)
                        .representativeName("rep-" + runId)
                        .businessRegistrationNumber("BRN-" + runId)
                        .foundationType(FoundationType.ETC)
                        .accountStatus(AccountStatus.ACTIVE)
                        .reviewStatus(ReviewStatus.APPROVED)
                        .feeRate(new BigDecimal("10"))
                        .account("110-123-123456")
                        .bankName("MANUAL")
                        .build()
        );
        Wallet foundationWallet = createWallet(WalletType.FOUNDATION, foundation.getFoundationNo(), WalletStatus.ACTIVE);
        walletOwnerBindingService.bindFoundationWallet(foundation.getFoundationNo(), foundationWallet.getWalletNo());

        Wallet campaignWallet1 = createWallet(WalletType.CAMPAIGN, foundation.getFoundationNo(), WalletStatus.INACTIVE);
        Wallet campaignWallet2 = createWallet(WalletType.CAMPAIGN, foundation.getFoundationNo(), WalletStatus.INACTIVE);
        Wallet campaignWallet3 = createWallet(WalletType.CAMPAIGN, foundation.getFoundationNo(), WalletStatus.INACTIVE);
        walletOwnerBindingService.bindFoundationCampaignWallets(
                foundation.getFoundationNo(),
                campaignWallet1.getWalletAddress(),
                campaignWallet2.getWalletAddress(),
                campaignWallet3.getWalletAddress()
        );

        preFundGasFromOwner(
                ownerWallet,
                ownerPrivateKey,
                userWallet.getWalletAddress(),
                beneficiaryWallet.getWalletAddress(),
                campaignWallet1.getWalletAddress()
        );

        Campaign campaign = campaignRepository.save(
                Campaign.builder()
                        .title("campaign-" + runId)
                        .description("manual real-chain e2e")
                        .category(CampaignCategory.ETC)
                        .targetAmount(10L)
                        .currentAmount(BigDecimal.ZERO)
                        .approvalStatus(ApprovalStatus.APPROVED)
                        .campaignStatus(CampaignStatus.ENDED)
                        .startAt(LocalDateTime.now().minusDays(3))
                        .endAt(LocalDateTime.now().minusDays(1))
                        .usageStartAt(LocalDateTime.now().minusHours(12))
                        .usageEndAt(LocalDateTime.now().plusDays(7))
                        .foundationNo(foundation.getFoundationNo())
                        .beneficiaryNo(beneficiary.getBeneficiaryNo())
                        .walletNo(campaignWallet1.getWalletNo())
                        .build()
        );

        BlockchainTransferResponse chargeResponse = blockchainTransferService.chargeUserToken(
                user.getUserNo(),
                5L,
                System.currentTimeMillis()
        );
        assertEquals("SUCCESS", chargeResponse.status(), "chargeUserToken failed");

        BlockchainTransferResponse donationResponse = blockchainTransferService.transferDonationToCampaign(
                user.getUserNo(),
                campaign.getCampaignNo(),
                3L,
                System.currentTimeMillis() + 1
        );
        assertEquals("SUCCESS", donationResponse.status(), "transferDonationToCampaign failed");

        BigDecimal campaignOnChainBalance = tokenAmountConverter.fromOnChainAmount(
                blockchainService.getTokenBalance(campaignWallet1.getWalletAddress())
        );
        assertTrue(campaignOnChainBalance.compareTo(new BigDecimal("3")) >= 0, "campaign on-chain token balance is insufficient");
        campaignWallet1.updateBalance(campaignOnChainBalance);
        campaignWallet1.updateLastUsedAt();
        walletRepository.save(campaignWallet1);

        settlementTransactionService.processSettlement(campaign);

        Wallet refreshedBeneficiaryWallet = walletLookupRepository.findByWalletTypeAndOwnerNo(
                        WalletType.BENEFICIARY,
                        beneficiary.getBeneficiaryNo()
                )
                .orElseThrow(() -> new IllegalStateException("beneficiary wallet missing after settlement"));
        assertNotNull(refreshedBeneficiaryWallet.getBalance(), "beneficiary balance not synchronized after settlement");
        assertTrue(refreshedBeneficiaryWallet.getBalance().compareTo(BigDecimal.ONE) >= 0, "beneficiary balance too low after settlement");

        RedemptionRequest redemptionRequest = new RedemptionRequest();
        ReflectionTestUtils.setField(redemptionRequest, "amount", 1L);
        RedemptionResponse redemptionResponse = redemptionService.requestRedemption(
                redemptionRequest,
                RequesterType.BENEFICIARY,
                beneficiary.getBeneficiaryNo()
        );
        assertEquals(RedemptionStatus.COMPLETED, redemptionResponse.getStatus(), "redemption failed");

        redemptionRepository.findById(redemptionResponse.getRedemptionNo())
                .orElseThrow(() -> new IllegalStateException("saved redemption not found"));

        List<Transaction> settlementTransactions = transactionRepository.findByWalletAddressAndStatus(
                campaignWallet1.getWalletAddress(),
                TransactionStatus.SUCCESS
        ).stream()
                .filter(tx -> tx.getEventType() == TransactionEventType.SETTLEMENT_FEE
                        || tx.getEventType() == TransactionEventType.SETTLEMENT_BENEFICIARY)
                .sorted(Comparator.comparing(Transaction::getTransactionNo).reversed())
                .toList();
        assertFalse(settlementTransactions.isEmpty(), "settlement transactions not found");

        List<Transaction> redemptionTransactions = transactionRepository.findByWalletAddressAndStatus(
                refreshedBeneficiaryWallet.getWalletAddress(),
                TransactionStatus.SUCCESS
        ).stream()
                .filter(tx -> tx.getEventType() == TransactionEventType.REDEMPTION)
                .sorted(Comparator.comparing(Transaction::getTransactionNo).reversed())
                .toList();
        assertFalse(redemptionTransactions.isEmpty(), "redemption transaction not found");

        String settlementTxHash = settlementTransactions.get(0).getTxHash();
        String redemptionTxHash = redemptionTransactions.get(0).getTxHash();

        System.out.println("REAL_CHAIN_E2E_RESULT");
        System.out.println("runId=" + runId);
        System.out.println("ownerAddress=" + ownerWallet.getWalletAddress());
        System.out.println("hotAddress=" + hotWallet.getWalletAddress());
        System.out.println("userWalletAddress=" + userWallet.getWalletAddress());
        System.out.println("campaignWalletAddress=" + campaignWallet1.getWalletAddress());
        System.out.println("foundationWalletAddress=" + foundationWallet.getWalletAddress());
        System.out.println("beneficiaryWalletAddress=" + refreshedBeneficiaryWallet.getWalletAddress());
        System.out.println("chargeTxHash=" + chargeResponse.txHash());
        System.out.println("donationTxHash=" + donationResponse.txHash());
        System.out.println("settlementTxHash=" + settlementTxHash);
        System.out.println("redemptionTxHash=" + redemptionTxHash);
        System.out.println("beneficiaryNo=" + beneficiary.getBeneficiaryNo());
        System.out.println("campaignNo=" + campaign.getCampaignNo());
    }

    private Wallet createWallet(WalletType walletType, Long ownerNo, WalletStatus status) {
        WalletCredentials credentials = walletCryptoService.createWalletCredentials();
        Key key = keyRepository.save(
                Key.builder()
                        .privateKey(credentials.encryptedPrivateKey())
                        .build()
        );

        return walletRepository.save(
                Wallet.builder()
                        .key(key)
                        .walletType(walletType)
                        .ownerNo(ownerNo)
                        .walletAddress(credentials.address())
                        .balance(BigDecimal.ZERO)
                        .walletHash(null)
                        .status(status)
                        .createdAt(LocalDateTime.now())
                        .lastUsedAt(LocalDateTime.now())
                        .build()
        );
    }

    private void preFundGasFromOwner(
            Wallet ownerWallet,
            String ownerPrivateKey,
            String userWalletAddress,
            String beneficiaryWalletAddress,
            String campaignWalletAddress
    ) throws Exception {
        BigInteger ownerBalanceWei = fetchNativeBalanceWei(ownerWallet.getWalletAddress());
        BigInteger gasPriceWei = fetchGasPriceWei();
        List<GasTopupTarget> targets = List.of(
                new GasTopupTarget(
                        userWalletAddress,
                        resolveTopupAmount(
                                "user donation",
                                gasPriceWei,
                                BigInteger.valueOf(transferTxGasLimit),
                                USER_GAS_TOPUP_WEI
                        )
                ),
                new GasTopupTarget(
                        beneficiaryWalletAddress,
                        resolveTopupAmount(
                                "beneficiary redemption",
                                gasPriceWei,
                                BigInteger.valueOf(contractTxGasLimit),
                                CONTRACT_GAS_TOPUP_WEI
                        )
                ),
                new GasTopupTarget(
                        campaignWalletAddress,
                        resolveTopupAmount(
                                "campaign settlement",
                                gasPriceWei,
                                BigInteger.valueOf(contractTxGasLimit),
                                CONTRACT_GAS_TOPUP_WEI
                        )
                )
        );

        BigInteger transferGasTotalWei = gasPriceWei
                .multiply(NATIVE_TRANSFER_GAS_LIMIT)
                .multiply(BigInteger.valueOf(targets.size()));
        BigInteger topupTotalWei = targets.stream()
                .map(GasTopupTarget::amountWei)
                .reduce(BigInteger.ZERO, BigInteger::add);
        BigInteger requiredTotalWei = OWNER_GAS_RESERVE_WEI
                .add(transferGasTotalWei)
                .add(topupTotalWei);

        assertTrue(
                ownerBalanceWei.compareTo(requiredTotalWei) >= 0,
                "owner native balance is insufficient for gas pre-funding. ownerBalanceWei=" + ownerBalanceWei
                        + ", requiredTotalWei=" + requiredTotalWei
                        + ", gasPriceWei=" + gasPriceWei
        );

        for (GasTopupTarget target : targets) {
            TransferResult result = blockchainTransferClient.transferNative(
                    ownerWallet.getWalletAddress(),
                    ownerPrivateKey,
                    target.address(),
                    target.amountWei()
            );
            assertEquals(
                    "SUCCESS",
                    result.status(),
                    "gas top-up failed: " + target.address() + ", " + result.message()
            );
        }
    }

    private BigInteger resolveTopupAmount(
            String label,
            BigInteger gasPriceWei,
            BigInteger gasLimit,
            BigInteger configuredTopupWei
    ) {
        BigInteger txCostWei = gasPriceWei.multiply(gasLimit);
        BigInteger bufferedCostWei = txCostWei.multiply(GAS_BUFFER_NUMERATOR).divide(GAS_BUFFER_DENOMINATOR);
        BigInteger neededWei = bufferedCostWei.add(TX_HEADROOM_WEI);
        assertTrue(
                configuredTopupWei.compareTo(neededWei) >= 0,
                label + " wallet needs more than configured top-up. configuredTopupWei=" + configuredTopupWei
                        + ", requiredWei=" + neededWei
                        + ", gasPriceWei=" + gasPriceWei
                        + ", gasLimit=" + gasLimit
        );
        return configuredTopupWei;
    }

    private BigInteger fetchNativeBalanceWei(String walletAddress) throws Exception {
        return web3j.ethGetBalance(walletAddress, DefaultBlockParameterName.PENDING)
                .send()
                .getBalance();
    }

    private BigInteger fetchGasPriceWei() throws Exception {
        return web3j.ethGasPrice()
                .send()
                .getGasPrice();
    }

    private String resolvePrivateKey(Wallet wallet) {
        if (wallet.getKey() == null || wallet.getKey().getKeyNo() == null) {
            throw new IllegalStateException("wallet key reference is missing. walletNo=" + wallet.getWalletNo());
        }

        Key key = keyRepository.findById(wallet.getKey().getKeyNo())
                .orElseThrow(() -> new IllegalStateException("key row not found: " + wallet.getKey().getKeyNo()));
        String storedPrivateKey = key.getPrivateKey();
        if (storedPrivateKey == null || storedPrivateKey.isBlank()) {
            throw new IllegalStateException("private key is empty. keyNo=" + key.getKeyNo());
        }

        String resolved;
        try {
            resolved = walletCryptoService.decryptPrivateKey(storedPrivateKey);
        } catch (RuntimeException e) {
            resolved = storedPrivateKey;
        }

        if (!EVM_PRIVATE_KEY.matcher(resolved).matches()) {
            throw new IllegalStateException("resolved private key is not valid hex. keyNo=" + key.getKeyNo());
        }
        return resolved;
    }

    private String uniquePhone() {
        long suffix = Math.abs(System.nanoTime() % 100000000L);
        return String.format("010%08d", suffix);
    }

    private record GasTopupTarget(String address, BigInteger amountWei) {
    }
}

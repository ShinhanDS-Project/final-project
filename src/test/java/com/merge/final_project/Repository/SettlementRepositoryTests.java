package com.merge.final_project.Repository;

import com.merge.final_project.blockchain.entity.Key;
import com.merge.final_project.blockchain.entity.Transaction;
import com.merge.final_project.blockchain.entity.TransactionEventType;
import com.merge.final_project.blockchain.entity.TransactionStatus;
import com.merge.final_project.blockchain.repository.KeyRepository;
import com.merge.final_project.blockchain.repository.TransactionRepository;
import com.merge.final_project.blockchain.service.BlockchainService;
import com.merge.final_project.blockchain.service.SettlementTransactionService;
import com.merge.final_project.campaign.campaigns.ApprovalStatus;
import com.merge.final_project.campaign.campaigns.entity.Campaign;
import com.merge.final_project.campaign.campaigns.repository.CampaignRepository;
import com.merge.final_project.campaign.campaigns.CampaignStatus;
import com.merge.final_project.campaign.settlement.Repository.SettlementRepository;
import com.merge.final_project.campaign.settlement.Settlement;
import com.merge.final_project.campaign.settlement.SettlementStatus;
import com.merge.final_project.org.foundation.Foundation;
import com.merge.final_project.org.foundation.FoundationRepository;
import com.merge.final_project.recipient.beneficiary.Beneficiary;
import com.merge.final_project.recipient.beneficiary.BeneficiaryRepository;
import com.merge.final_project.recipient.beneficiary.BeneficiaryType;
import com.merge.final_project.wallet.entity.Wallet;
import com.merge.final_project.wallet.entity.WalletStatus;
import com.merge.final_project.wallet.entity.WalletType;
import com.merge.final_project.wallet.repository.WalletRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.web3j.protocol.core.methods.response.TransactionReceipt;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringBootTest
public class SettlementRepositoryTests {
    @TestConfiguration
    static class TestConfig {
        @Bean
        @Primary
        BlockchainService blockchainService() {
            return mock(BlockchainService.class);
        }
    }

    @Autowired
    private WalletRepository walletRepository;
    @Autowired
    private KeyRepository keyRepository;
    @Autowired
    private FoundationRepository foundationRepository;
    @Autowired
    private BeneficiaryRepository beneficiaryRepository;
    @Autowired
    private CampaignRepository campaignRepository;
    @Autowired
    private BlockchainService blockchainService;
    @Autowired
    private SettlementTransactionService settlementTransactionService;
    @Autowired
    private SettlementRepository settlementRepository;
    @Autowired
    private TransactionRepository transactionRepository;

    @Test
    public void insertWallet() {

        Key key3 = Key.builder()
                .privateKey("dummy-private-key-3")
                .aesKey("dummy-aes-key-3")
                .build();

        keyRepository.save(key3);

        Key key4 = Key.builder()
                .privateKey("dummy-private-key-4")
                .aesKey("dummy-aes-key-4")
                .build();

        keyRepository.save(key4);

        Wallet beneficiaryWallet = Wallet.builder()
                .walletType(WalletType.BENEFICIARY)
                .key(key3)
                .ownerNo(null)
                .walletAddress("0xbeneficiary_" + UUID.randomUUID())
                .balance(new BigDecimal("0"))
                .walletHash("hash_beneficiary_123")
                .status(WalletStatus.ACTIVE)
                .campaign(null)
                .createdAt(LocalDateTime.now())
                .build();

        walletRepository.save(beneficiaryWallet);

        Wallet foundationWallet = Wallet.builder()
                .walletType(WalletType.FOUNDATION)
                .key(key4)
                .ownerNo(null)
                .walletAddress("0xfoundation_" + UUID.randomUUID())
                .balance(new BigDecimal("0"))
                .walletHash("hash_foundation_123")
                .status(WalletStatus.ACTIVE)
                .campaign(null)
                .createdAt(LocalDateTime.now())
                .build();

        walletRepository.save(foundationWallet);

    }

    @Test
    public void insertBeneficiary() {
        Wallet beneficiaryWallet = walletRepository.findById(3L)
                .orElseThrow(() -> new RuntimeException("수혜자 지갑 없음"));

        Beneficiary beneficiary = Beneficiary.builder()
                .name("희망아동센터")
                .email("beneficiary@test.com")
                .password("1234")
                .phone("010-1111-2222")
                .account("110-123-456789")
                .beneficiaryHash("hash_beneficiary_001")
                .beneficiaryType(BeneficiaryType.INDIVIDUAL)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .wallet(beneficiaryWallet)
                .entryCode(11111)
                .build();

        beneficiaryRepository.save(beneficiary);
    }

    @Test
    public void insertFoundation() {
        Wallet foundationWallet = walletRepository.findById(4L)
                .orElseThrow(() -> new RuntimeException("기부단체 지갑 없음"));

        Foundation foundation = Foundation.builder()
                .foundationHash("hash_foundation_001")
                .foundationName("굿네이버재단")
                .businessRegistrationNumber("1234654")
                .representativeName("홍길동")
                .contactPhone("02-1234-5678")
                .description("아동 및 취약계층 지원 단체")
                .feeRate(new BigDecimal("0.05"))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .profilePath("/images/foundation1.png")
                .account("222-333-444444")
                .foundationEmail("foundation@test.com")
                .wallet(foundationWallet)
                .build();

        foundationRepository.save(foundation);
    }

    @Test
    public void createCampaignWithWallet() {

        Key key = keyRepository.save(
                Key.builder()
                        .privateKey("campaign-private-key")
                        .build()
        );

        Wallet campaignWallet = walletRepository.save(
                Wallet.builder()
                        .walletType(WalletType.CAMPAIGN)
                        .key(key)
                        .walletAddress("0xcampaign_wallet_" + UUID.randomUUID())
                        .balance(BigDecimal.ZERO)
                        .walletHash("hash_campaign_wallet_001")
                        .status(WalletStatus.ACTIVE)
                        .createdAt(LocalDateTime.now())
                        .build()
        );

        Campaign campaign = Campaign.builder()
                .title("테스트 캠페인")
                .description("DB 맞춘 캠페인")

                .imagePath("test.png")

                .startAt(LocalDateTime.of(2026, 4, 3, 10, 0, 0))
                .endAt(LocalDateTime.of(2026, 5, 3, 10, 0, 0))
                .usageStartAt(LocalDateTime.of(2026, 5, 4, 10, 0, 0))
                .usageEndAt(LocalDateTime.of(2026, 6, 4, 10, 0, 0))

                .targetAmount(1000000L)
                .currentAmount(0L)
                .achievedAt(null)

                .category("DONATION")

                .approvalStatus(ApprovalStatus.APPROVED)
                .approvedAt(LocalDateTime.of(2026, 4, 3, 10, 0, 0))

                .campaignStatus(CampaignStatus.ACTIVE)

                // 🔥 핵심 (String으로 변환)
                .walletNo(campaignWallet.getWalletNo())
                .foundationNo(1L)
                .beneficiaryNo(1L)

                .createdAt(LocalDateTime.of(2026, 4, 3, 10, 0, 0))
                .updatedAt(LocalDateTime.of(2026, 4, 3, 10, 0, 0))

                .rejectReason(null)

                .build();

        campaignRepository.save(campaign);

        System.out.println("캠페인 + 지갑 생성 완료 🔥");
    }

    @Test
    @DisplayName("정산 성공 시 settlement, transaction, campaign, wallet 상태가 정상 반영된다")
    void processSettlement_success() throws Exception {
        // given
        Key campaignKey = keyRepository.save(
                Key.builder()
                        .privateKey("campaign-private-key")
                        .build()
        );

        Key foundationKey = keyRepository.save(
                Key.builder()
                        .privateKey("foundation-private-key")
                        .build()
        );

        Key beneficiaryKey = keyRepository.save(
                Key.builder()
                        .privateKey("beneficiary-private-key")
                        .build()
        );

        Wallet campaignWallet = walletRepository.save(
                Wallet.builder()
                        .walletAddress("0xCampaignWallet_" + UUID.randomUUID())
                        .balance(new BigDecimal("1000"))
                        .key(campaignKey)
                        .build()
        );

        Wallet foundationWallet = walletRepository.save(
                Wallet.builder()
                        .walletAddress("0xFoundationWallet_" + UUID.randomUUID())
                        .balance(BigDecimal.ZERO)
                        .key(foundationKey)
                        .build()
        );

        Wallet beneficiaryWallet = walletRepository.save(
                Wallet.builder()
                        .walletAddress("0xBeneficiaryWallet_" + UUID.randomUUID())
                        .balance(BigDecimal.ZERO)
                        .key(beneficiaryKey)
                        .build()
        );

        Foundation foundation = foundationRepository.save(
                Foundation.builder()
                        .foundationName("테스트 기부단체")
                        .foundationEmail("test@test.com")
                        .feeRate(new BigDecimal("10")) // 10%
                        .wallet(foundationWallet)
                        .build()
        );

        Beneficiary beneficiary = beneficiaryRepository.save(
                Beneficiary.builder()
                        .name("테스트 수혜자")
                        .email("beneficiary_" + UUID.randomUUID() + "@test.com")
                        .password("1234")
                        .entryCode(1111)
                        .phone("01012345678")
                        .beneficiaryType(BeneficiaryType.ORGANIZATION)
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .wallet(beneficiaryWallet)
                        .build()
        );

        Campaign campaign = campaignRepository.save(
                Campaign.builder()
                        .title("테스트 캠페인")
                        .achievedAt(null)
                        .createdAt(LocalDateTime.now())
                        .campaignStatus(CampaignStatus.ENDED)
                        .walletNo(campaignWallet.getWalletNo())
                        .foundationNo(foundation.getFoundationNo())
                        .beneficiaryNo(beneficiary.getBeneficiaryNo())
                        .build()
        );

        TransactionReceipt receipt = new TransactionReceipt();
        receipt.setTransactionHash("0xTEST_HASH");
        receipt.setBlockNumber("123");
        receipt.setGasUsed("21000");

        when(blockchainService.settleCampaignOnChain(
                any(), any(), any(), any(), any(), any(), any()
        )).thenReturn(receipt);
        when(blockchainService.getTokenBalance(campaignWallet.getWalletAddress()))
                .thenReturn(BigDecimal.ZERO.toBigInteger());
        when(blockchainService.getTokenBalance(foundationWallet.getWalletAddress()))
                .thenReturn(BigInteger.valueOf(100));
        when(blockchainService.getTokenBalance(beneficiaryWallet.getWalletAddress()))
                .thenReturn(BigInteger.valueOf(900));

        // when
        settlementTransactionService.processSettlement(campaign);

        // then
        List<Settlement> settlements = settlementRepository.findByCampaign(campaign);
        assertThat(settlements).hasSize(1);

        Settlement settlement = settlements.get(0);
        assertThat(settlement.getStatus()).isEqualTo(SettlementStatus.COMPLETED);
        assertThat(settlement.getAmount()).isEqualTo(1000);
        assertThat(settlement.getFoundationAmount()).isEqualTo(100);
        assertThat(settlement.getBeneficiaryAmount()).isEqualTo(900);
        assertThat(settlement.getSettledAt()).isNotNull();

        List<Transaction> transactions = transactionRepository.findByTransactionCode(settlement.getTransactionCode());
        assertThat(transactions).hasSize(2);

        assertThat(transactions)
                .extracting(Transaction::getEventType)
                .containsExactlyInAnyOrder(
                        TransactionEventType.SETTLEMENT_FEE,
                        TransactionEventType.SETTLEMENT_BENEFICIARY
                );

        assertThat(transactions)
                .extracting(Transaction::getStatus)
                .containsOnly(TransactionStatus.SUCCESS);

        Campaign savedCampaign = campaignRepository.findById(campaign.getCampaignNo()).orElseThrow();
        assertThat(savedCampaign.getCampaignStatus()).isEqualTo(CampaignStatus.SETTLED);

        Wallet savedCampaignWallet = walletRepository.findById(campaignWallet.getWalletNo()).orElseThrow();
        assertThat(savedCampaignWallet.getBalance()).isEqualByComparingTo(BigDecimal.ZERO);

        Wallet savedFoundationWallet = walletRepository.findById(foundationWallet.getWalletNo()).orElseThrow();
        assertThat(savedFoundationWallet.getBalance()).isEqualByComparingTo(new BigDecimal("100"));

        Wallet savedBeneficiaryWallet = walletRepository.findById(beneficiaryWallet.getWalletNo()).orElseThrow();
        assertThat(savedBeneficiaryWallet.getBalance()).isEqualByComparingTo(new BigDecimal("900"));
    }

    @Test
    @DisplayName("블록체인 호출 실패 시 settlement는 FAILED 상태가 된다")
    void processSettlement_fail() throws Exception {
        // given
        Key campaignKey = keyRepository.save(
                Key.builder()
                        .privateKey("campaign-private-key")
                        .build()
        );

        Key foundationKey = keyRepository.save(
                Key.builder()
                        .privateKey("foundation-private-key")
                        .build()
        );

        Key beneficiaryKey = keyRepository.save(
                Key.builder()
                        .privateKey("beneficiary-private-key")
                        .build()
        );

        Wallet campaignWallet = walletRepository.save(
                Wallet.builder()
                        .walletAddress("0xCampaignWallet_" + UUID.randomUUID())
                        .balance(new BigDecimal("1000"))
                        .key(campaignKey)
                        .build()
        );

        Wallet foundationWallet = walletRepository.save(
                Wallet.builder()
                        .walletAddress("0xFoundationWallet_" + UUID.randomUUID())
                        .balance(BigDecimal.ZERO)
                        .key(foundationKey)
                        .build()
        );

        Wallet beneficiaryWallet = walletRepository.save(
                Wallet.builder()
                        .walletAddress("0xBeneficiaryWallet_" + UUID.randomUUID())
                        .balance(BigDecimal.ZERO)
                        .key(beneficiaryKey)
                        .build()
        );

        Foundation foundation = foundationRepository.save(
                Foundation.builder()
                        .foundationName("테스트 기부단체")
                        .foundationEmail("test@test.com")
                        .feeRate(new BigDecimal("10")) // 10%
                        .wallet(foundationWallet)
                        .build()
        );

        Beneficiary beneficiary = beneficiaryRepository.save(
                Beneficiary.builder()
                        .name("테스트 수혜자")
                        .email("beneficiary_" + UUID.randomUUID() + "@test.com")
                        .password("1234")
                        .entryCode(1111)
                        .phone("01012345678")
                        .beneficiaryType(BeneficiaryType.ORGANIZATION)
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .wallet(beneficiaryWallet)
                        .build()
        );

        Campaign campaign = campaignRepository.save(
                Campaign.builder()
                        .title("테스트 캠페인")
                        .achievedAt(null)
                        .createdAt(LocalDateTime.now())
                        .campaignStatus(CampaignStatus.ENDED)
                        .walletNo(campaignWallet.getWalletNo())
                        .foundationNo(foundation.getFoundationNo())
                        .beneficiaryNo(beneficiary.getBeneficiaryNo())
                        .build()
        );

        when(blockchainService.settleCampaignOnChain(
                any(), any(), any(), any(), any(), any(), any()
        )).thenThrow(new RuntimeException("온체인 호출 실패"));

        // when
        try {
            settlementTransactionService.processSettlement(campaign);
        } catch (Exception ignored) {
        }

        // then
        List<Settlement> settlements = settlementRepository.findByCampaign(campaign);
        assertThat(settlements).hasSize(1);

        Settlement settlement = settlements.get(0);
        assertThat(settlement.getStatus()).isEqualTo(SettlementStatus.FAILED);

        List<Transaction> transactions = transactionRepository.findByTransactionCode(settlement.getTransactionCode());
        assertThat(transactions).isEmpty();

        Campaign savedCampaign = campaignRepository.findById(campaign.getCampaignNo()).orElseThrow();
        assertThat(savedCampaign.getCampaignStatus()).isEqualTo(CampaignStatus.ENDED);
    }



}

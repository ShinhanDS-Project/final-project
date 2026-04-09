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
import com.merge.final_project.org.Foundation;
import com.merge.final_project.org.FoundationRepository;

import com.merge.final_project.recipient.beneficiary.BeneficiaryType;
import com.merge.final_project.recipient.beneficiary.entity.Beneficiary;
import com.merge.final_project.recipient.beneficiary.repository.BeneficiaryRepository;
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
    @DisplayName("정산 성공 시 settlement, transaction, campaign, wallet 상태가 정상 반영된다")
    void processSettlement_success() throws Exception {
        Wallet campaignWallet = saveWallet("campaign-private-key", "0xCampaignWallet_", new BigDecimal("1000"));
        Wallet foundationWallet = saveWallet("foundation-private-key", "0xFoundationWallet_", BigDecimal.ZERO);
        Wallet beneficiaryWallet = saveWallet("beneficiary-private-key", "0xBeneficiaryWallet_", BigDecimal.ZERO);

        Foundation foundation = saveFoundation(foundationWallet, new BigDecimal("10"));
        Beneficiary beneficiary = saveBeneficiary(beneficiaryWallet);
        Campaign campaign = saveEndedCampaign(campaignWallet, foundation, beneficiary);

        TransactionReceipt receipt = new TransactionReceipt();
        receipt.setTransactionHash("0xTEST_HASH");
        receipt.setBlockNumber("0x7b");
        receipt.setGasUsed("0x5208");

        when(blockchainService.settleCampaignOnChain(
                any(), any(), any(), any(), any(), any(), any()
        )).thenReturn(receipt);
        when(blockchainService.getTokenBalance(campaignWallet.getWalletAddress()))
                .thenReturn(BigInteger.valueOf(1000), BigInteger.ZERO);
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
        assertThat(settlement.getAmount()).isEqualTo(1000L);
        assertThat(settlement.getFoundationAmount()).isEqualTo(100L);
        assertThat(settlement.getBeneficiaryAmount()).isEqualTo(900L);
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
        Wallet campaignWallet = saveWallet("campaign-private-key", "0xCampaignWallet_", new BigDecimal("1000"));
        Wallet foundationWallet = saveWallet("foundation-private-key", "0xFoundationWallet_", BigDecimal.ZERO);
        Wallet beneficiaryWallet = saveWallet("beneficiary-private-key", "0xBeneficiaryWallet_", BigDecimal.ZERO);

        Foundation foundation = saveFoundation(foundationWallet, new BigDecimal("10"));
        Beneficiary beneficiary = saveBeneficiary(beneficiaryWallet);
        Campaign campaign = saveEndedCampaign(campaignWallet, foundation, beneficiary);

        when(blockchainService.settleCampaignOnChain(
                any(), any(), any(), any(), any(), any(), any()
        )).thenThrow(new RuntimeException("온체인 정산 실패"));
        when(blockchainService.getTokenBalance(campaignWallet.getWalletAddress()))
                .thenReturn(BigInteger.valueOf(1000));

        try {
            settlementTransactionService.processSettlement(campaign);
        } catch (Exception ignored) {
        }

        List<Settlement> settlements = settlementRepository.findByCampaign(campaign);
        assertThat(settlements).hasSize(1);

        Settlement settlement = settlements.get(0);
        assertThat(settlement.getStatus()).isEqualTo(SettlementStatus.FAILED);

        List<Transaction> transactions = transactionRepository.findByTransactionCode(settlement.getTransactionCode());
        assertThat(transactions).isEmpty();

        Campaign savedCampaign = campaignRepository.findById(campaign.getCampaignNo()).orElseThrow();
        assertThat(savedCampaign.getCampaignStatus()).isEqualTo(CampaignStatus.ENDED);
    }

    private Wallet saveWallet(String privateKey, String addressPrefix, BigDecimal balance) {
        Key key = keyRepository.save(
                Key.builder()
                        .privateKey(privateKey)
                        .build()
        );

        return walletRepository.save(
                Wallet.builder()
                        .walletAddress(addressPrefix + UUID.randomUUID())
                        .balance(balance)
                        .key(key)
                        .build()
        );
    }

    private Foundation saveFoundation(Wallet foundationWallet, BigDecimal feeRate) {
        return foundationRepository.save(
                Foundation.builder()
                        .foundationName("테스트 기부단체")
                        .foundationEmail("foundation_" + UUID.randomUUID() + "@test.com")
                        .feeRate(feeRate)
                        .wallet(foundationWallet)
                        .build()
        );
    }

    private Beneficiary saveBeneficiary(Wallet beneficiaryWallet) {
        return beneficiaryRepository.save(
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
    }

    private Campaign saveEndedCampaign(Wallet campaignWallet, Foundation foundation, Beneficiary beneficiary) {
        return campaignRepository.save(
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
    }
}

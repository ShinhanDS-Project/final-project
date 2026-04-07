package com.merge.final_project.Repository;

import com.merge.final_project.blockchain.entity.Key;
import com.merge.final_project.blockchain.entity.Transaction;
import com.merge.final_project.blockchain.entity.TransactionEventType;
import com.merge.final_project.blockchain.entity.TransactionStatus;
import com.merge.final_project.blockchain.repository.KeyRepository;
import com.merge.final_project.blockchain.repository.TransactionRepository;
import com.merge.final_project.blockchain.service.BlockchainService;
import com.merge.final_project.org.foundation.Foundation;
import com.merge.final_project.org.foundation.FoundationRepository;
import com.merge.final_project.recipient.beneficiary.Beneficiary;
import com.merge.final_project.recipient.beneficiary.BeneficiaryRepository;
import com.merge.final_project.recipient.beneficiary.BeneficiaryType;
import com.merge.final_project.redemption.RedemptionStatus;
import com.merge.final_project.redemption.RequesterType;
import com.merge.final_project.redemption.dto.request.RedemptionRequest;
import com.merge.final_project.redemption.dto.response.RedemptionResponse;
import com.merge.final_project.redemption.entity.Redemption;
import com.merge.final_project.redemption.repository.RedemptionRepository;
import com.merge.final_project.redemption.service.RedemptionService;
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
import org.springframework.test.util.ReflectionTestUtils;
import org.web3j.protocol.core.methods.response.TransactionReceipt;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringBootTest
public class RedemptionRepositoryTests {

    @TestConfiguration
    static class TestConfig {
        @Bean
        @Primary
        BlockchainService blockchainService() {
            return mock(BlockchainService.class);
        }
    }

    @Autowired
    private BlockchainService blockchainService;
    @Autowired
    private RedemptionService redemptionService;
    @Autowired
    private RedemptionRepository redemptionRepository;
    @Autowired
    private TransactionRepository transactionRepository;
    @Autowired
    private WalletRepository walletRepository;
    @Autowired
    private KeyRepository keyRepository;
    @Autowired
    private FoundationRepository foundationRepository;
    @Autowired
    private BeneficiaryRepository beneficiaryRepository;

    @Test
    @DisplayName("재단 현금화 성공 시 redemption, transaction, wallet 상태가 정상 반영된다")
    void requestRedemption_foundation_success() throws Exception {
        Key requesterKey = saveKey("foundation-private-key");
        Wallet requesterWallet = saveWallet(
                WalletType.FOUNDATION,
                "0xFoundationWallet_" + UUID.randomUUID(),
                new BigDecimal("500"),
                requesterKey
        );
        Wallet hotWallet = createHotWallet();
        ReflectionTestUtils.setField(redemptionService, "hotWalletAddress", hotWallet.getWalletAddress());

        Foundation foundation = foundationRepository.save(
                Foundation.builder()
                        .foundationName("테스트 재단")
                        .foundationEmail("foundation_" + UUID.randomUUID() + "@test.com")
                        .wallet(requesterWallet)
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .build()
        );

        RedemptionRequest request = createRequest(RequesterType.FOUNDATION, foundation.getFoundationNo(), 200L);

        TransactionReceipt receipt = new TransactionReceipt();
        receipt.setTransactionHash("0xREDEMPTION_SUCCESS_HASH");
        receipt.setBlockNumber("321");
        receipt.setGasUsed("21000");

        when(blockchainService.redeemOnChain(any(), any(), any())).thenReturn(receipt);
        when(blockchainService.getTokenBalance(requesterWallet.getWalletAddress()))
                .thenReturn(BigInteger.valueOf(300));
        when(blockchainService.getTokenBalance(hotWallet.getWalletAddress()))
                .thenReturn(BigInteger.valueOf(200));

        RedemptionResponse response = redemptionService.requestRedemption(request);

        Redemption redemption = findLatestRedemptionByRequesterNo(foundation.getFoundationNo());
        assertThat(redemption.getStatus()).isEqualTo(RedemptionStatus.COMPLETED);
        assertThat(redemption.getTransaction()).isNotNull();
        assertThat(redemption.getBlockNumber()).isEqualTo(321L);
        assertThat(redemption.getFailureReason()).isNull();
        assertThat(redemption.getProcessedAt()).isNotNull();

        Transaction transaction = transactionRepository.findById(redemption.getTransaction().getTransactionNo()).orElseThrow();
        assertThat(transaction.getEventType()).isEqualTo(TransactionEventType.REDEMPTION);
        assertThat(transaction.getStatus()).isEqualTo(TransactionStatus.SUCCESS);
        assertThat(transaction.getFromWallet().getWalletNo()).isEqualTo(requesterWallet.getWalletNo());
        assertThat(transaction.getToWallet().getWalletNo()).isEqualTo(hotWallet.getWalletNo());
        assertThat(transaction.getTxHash()).isEqualTo("0xREDEMPTION_SUCCESS_HASH");
        assertThat(transaction.getBlockNum()).isEqualTo(321L);

        Wallet savedRequesterWallet = walletRepository.findById(requesterWallet.getWalletNo()).orElseThrow();
        assertThat(savedRequesterWallet.getBalance()).isEqualByComparingTo(new BigDecimal("300"));
        assertThat(savedRequesterWallet.getLastUsedAt()).isNotNull();

        Wallet savedHotWallet = walletRepository.findById(hotWallet.getWalletNo()).orElseThrow();
        assertThat(savedHotWallet.getBalance()).isEqualByComparingTo(new BigDecimal("200"));
        assertThat(savedHotWallet.getLastUsedAt()).isNotNull();

        assertThat(response.getStatus()).isEqualTo(RedemptionStatus.COMPLETED);
        assertThat(response.getAmount()).isEqualTo(200L);
        assertThat(response.getRequesterNo()).isEqualTo(foundation.getFoundationNo());
    }

    @Test
    @DisplayName("수혜자 현금화 중 블록체인 호출이 실패하면 redemption 은 FAILED 이고 transaction 은 저장되지 않는다")
    void requestRedemption_beneficiary_fail() throws Exception {
        Key requesterKey = saveKey("beneficiary-private-key");
        Wallet requesterWallet = saveWallet(
                WalletType.BENEFICIARY,
                "0xBeneficiaryWallet_" + UUID.randomUUID(),
                new BigDecimal("700"),
                requesterKey
        );
        Wallet hotWallet = createHotWallet();
        ReflectionTestUtils.setField(redemptionService, "hotWalletAddress", hotWallet.getWalletAddress());

        Beneficiary beneficiary = beneficiaryRepository.save(
                Beneficiary.builder()
                        .name("테스트 수혜자")
                        .email("beneficiary_" + UUID.randomUUID() + "@test.com")
                        .password("1234")
                        .entryCode(1234)
                        .phone("01012345678")
                        .beneficiaryType(BeneficiaryType.INDIVIDUAL)
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .wallet(requesterWallet)
                        .build()
        );

        RedemptionRequest request = createRequest(RequesterType.BENEFICIARY, beneficiary.getBeneficiaryNo(), 250L);
        long transactionCountBefore = transactionRepository.count();

        when(blockchainService.redeemOnChain(any(), any(), any()))
                .thenThrow(new RuntimeException("블록체인 현금화 실패"));

        try {
            redemptionService.requestRedemption(request);
        } catch (Exception ignored) {
        }

        Redemption redemption = findLatestRedemptionByRequesterNo(beneficiary.getBeneficiaryNo());
        assertThat(redemption.getStatus()).isEqualTo(RedemptionStatus.FAILED);
        assertThat(redemption.getTransaction()).isNull();
        assertThat(redemption.getFailureReason()).isEqualTo("블록체인 현금화 처리에 실패했습니다.");
        assertThat(redemption.getProcessedAt()).isNotNull();

        assertThat(transactionRepository.count()).isEqualTo(transactionCountBefore);

        Wallet savedRequesterWallet = walletRepository.findById(requesterWallet.getWalletNo()).orElseThrow();
        assertThat(savedRequesterWallet.getBalance()).isEqualByComparingTo(new BigDecimal("700"));
    }

    private RedemptionRequest createRequest(RequesterType requesterType, Long requesterNo, Long amount) {
        RedemptionRequest request = new RedemptionRequest();
        ReflectionTestUtils.setField(request, "requesterType", requesterType);
        ReflectionTestUtils.setField(request, "requesterNo", requesterNo);
        ReflectionTestUtils.setField(request, "amount", amount);
        return request;
    }

    private Key saveKey(String privateKey) {
        return keyRepository.save(
                Key.builder()
                        .privateKey(privateKey)
                        .build()
        );
    }

    private Wallet saveWallet(WalletType walletType, String walletAddress, BigDecimal balance, Key key) {
        return walletRepository.save(
                Wallet.builder()
                        .walletType(walletType)
                        .walletAddress(walletAddress)
                        .balance(balance)
                        .key(key)
                        .status(WalletStatus.ACTIVE)
                        .createdAt(LocalDateTime.now())
                        .build()
        );
    }

    private Wallet createHotWallet() {
        return saveWallet(
                WalletType.HOT,
                "0xHotWallet_" + UUID.randomUUID(),
                BigDecimal.ZERO,
                saveKey("hot-wallet-private-key-" + UUID.randomUUID())
        );
    }

    private Redemption findLatestRedemptionByRequesterNo(Long requesterNo) {
        return redemptionRepository.findAll().stream()
                .filter(redemption -> requesterNo.equals(redemption.getRequesterNo()))
                .max(Comparator.comparing(Redemption::getRedemptionNo))
                .orElseThrow();
    }
}

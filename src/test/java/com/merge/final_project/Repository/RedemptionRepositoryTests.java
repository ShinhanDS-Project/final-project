package com.merge.final_project.Repository;

import com.merge.final_project.blockchain.entity.Key;
import com.merge.final_project.blockchain.entity.Transaction;
import com.merge.final_project.blockchain.entity.TransactionEventType;
import com.merge.final_project.blockchain.entity.TransactionStatus;
import com.merge.final_project.blockchain.repository.KeyRepository;
import com.merge.final_project.blockchain.repository.TransactionRepository;
import com.merge.final_project.blockchain.service.BlockchainService;
import com.merge.final_project.org.Foundation;
import com.merge.final_project.org.FoundationRepository;
import com.merge.final_project.recipient.beneficiary.Beneficiary;
import com.merge.final_project.recipient.beneficiary.BeneficiaryRepository;
import com.merge.final_project.recipient.beneficiary.BeneficiaryType;
import com.merge.final_project.redemption.RedemptionStatus;
import com.merge.final_project.redemption.RequesterType;
import com.merge.final_project.redemption.dto.request.RedemptionRequest;
import com.merge.final_project.redemption.dto.response.RedemptionDetailResponse;
import com.merge.final_project.redemption.dto.response.RedemptionListResponse;
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
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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
                .thenReturn(BigInteger.valueOf(500))
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

        when(blockchainService.getTokenBalance(requesterWallet.getWalletAddress()))
                .thenReturn(BigInteger.valueOf(700));

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

    @Test
    @DisplayName("관리자 현금화 목록 조회 시 요청자명, 상태, txHash 를 확인할 수 있다")
    void getAdminRedemptions_success() throws Exception {
        Wallet hotWallet = createHotWallet();
        ReflectionTestUtils.setField(redemptionService, "hotWalletAddress", hotWallet.getWalletAddress());

        Foundation foundation = createFoundationWithWallet(new BigDecimal("500"));
        RedemptionRequest request = createRequest(RequesterType.FOUNDATION, foundation.getFoundationNo(), 200L);

        TransactionReceipt receipt = new TransactionReceipt();
        receipt.setTransactionHash("0xADMIN_LIST_HASH");
        receipt.setBlockNumber("321");
        receipt.setGasUsed("21000");

        when(blockchainService.redeemOnChain(any(), any(), any())).thenReturn(receipt);
        when(blockchainService.getTokenBalance(any()))
                .thenReturn(BigInteger.valueOf(500))
                .thenReturn(BigInteger.valueOf(300))
                .thenReturn(BigInteger.valueOf(200));

        RedemptionResponse response = redemptionService.requestRedemption(request);
        redemptionService.markCashPaid(response.getRedemptionNo());

        Foundation anotherFoundation = createFoundationWithWallet(new BigDecimal("600"));
        RedemptionRequest anotherRequest = createRequest(RequesterType.FOUNDATION, anotherFoundation.getFoundationNo(), 100L);

        TransactionReceipt anotherReceipt = new TransactionReceipt();
        anotherReceipt.setTransactionHash("0xADMIN_LIST_COMPLETED_HASH");
        anotherReceipt.setBlockNumber("322");
        anotherReceipt.setGasUsed("21000");

        when(blockchainService.redeemOnChain(any(), any(), any())).thenReturn(anotherReceipt);
        when(blockchainService.getTokenBalance(any()))
                .thenReturn(BigInteger.valueOf(600))
                .thenReturn(BigInteger.valueOf(500))
                .thenReturn(BigInteger.valueOf(100));

        RedemptionResponse completedResponse = redemptionService.requestRedemption(anotherRequest);
        List<RedemptionListResponse> results = redemptionService.getAdminRedemptions(null);

        System.out.println("=== COMPLETED redemption list ===");
        results.forEach(item -> System.out.println(
                "redemptionNo=" + item.getRedemptionNo()
                        + ", requesterName=" + item.getRequesterName()
                        + ", requesterType=" + item.getRequesterType()
                        + ", amount=" + item.getAmount()
                        + ", status=" + item.getStatus()
                        + ", requestedAt=" + item.getRequestedAt()
                        + ", txHash=" + item.getTxHash()
        ));

        RedemptionListResponse target = results.stream()
                .filter(item -> item.getRedemptionNo().equals(completedResponse.getRedemptionNo()))
                .findFirst()
                .orElseThrow();

        assertThat(results).allMatch(item -> item.getStatus() == RedemptionStatus.COMPLETED);
        assertThat(target.getRequesterName()).isEqualTo(anotherFoundation.getFoundationName());
        assertThat(target.getRequesterType()).isEqualTo(RequesterType.FOUNDATION.name());
        assertThat(target.getAmount()).isEqualTo(100L);
        assertThat(target.getStatus()).isEqualTo(RedemptionStatus.COMPLETED);
        assertThat(target.getTxHash()).isEqualTo("0xADMIN_LIST_COMPLETED_HASH");
    }

    @Test
    @DisplayName("관리자 현금화 목록은 상태 필터로 조회할 수 있다")
    void getAdminRedemptions_withStatusFilter_success() throws Exception {
        Wallet hotWallet = createHotWallet();
        ReflectionTestUtils.setField(redemptionService, "hotWalletAddress", hotWallet.getWalletAddress());

        Foundation foundation = createFoundationWithWallet(new BigDecimal("500"));
        RedemptionRequest request = createRequest(RequesterType.FOUNDATION, foundation.getFoundationNo(), 200L);

        TransactionReceipt receipt = new TransactionReceipt();
        receipt.setTransactionHash("0xADMIN_FILTER_HASH");
        receipt.setBlockNumber("333");
        receipt.setGasUsed("21000");

        when(blockchainService.redeemOnChain(any(), any(), any())).thenReturn(receipt);
        when(blockchainService.getTokenBalance(any()))
                .thenReturn(BigInteger.valueOf(500))
                .thenReturn(BigInteger.valueOf(300))
                .thenReturn(BigInteger.valueOf(200));

        RedemptionResponse response = redemptionService.requestRedemption(request);
        redemptionService.markCashPaid(response.getRedemptionNo());

        List<RedemptionListResponse> paidResults = redemptionService.getAdminRedemptions(RedemptionStatus.PAID);

        System.out.println("=== PAID redemption list ===");
        paidResults.forEach(item -> System.out.println(
                "redemptionNo=" + item.getRedemptionNo()
                        + ", requesterName=" + item.getRequesterName()
                        + ", requesterType=" + item.getRequesterType()
                        + ", amount=" + item.getAmount()
                        + ", status=" + item.getStatus()
                        + ", requestedAt=" + item.getRequestedAt()
                        + ", txHash=" + item.getTxHash()
        ));

        assertThat(paidResults).allMatch(item -> item.getStatus() == RedemptionStatus.PAID);
        assertThat(paidResults.stream().map(RedemptionListResponse::getRedemptionNo))
                .contains(response.getRedemptionNo());
    }

    @Test
    @DisplayName("관리자 현금화 상세 조회 시 계좌, 지갑, txHash 를 확인할 수 있다")
    void getAdminRedemptionDetail_success() throws Exception {
        Wallet hotWallet = createHotWallet();
        ReflectionTestUtils.setField(redemptionService, "hotWalletAddress", hotWallet.getWalletAddress());

        Beneficiary beneficiary = createBeneficiaryWithWallet(new BigDecimal("800"));
        RedemptionRequest request = createRequest(RequesterType.BENEFICIARY, beneficiary.getBeneficiaryNo(), 300L);

        TransactionReceipt receipt = new TransactionReceipt();
        receipt.setTransactionHash("0xADMIN_DETAIL_HASH");
        receipt.setBlockNumber("654");
        receipt.setGasUsed("21000");

        when(blockchainService.redeemOnChain(any(), any(), any())).thenReturn(receipt);
        when(blockchainService.getTokenBalance(any()))
                .thenReturn(BigInteger.valueOf(800))
                .thenReturn(BigInteger.valueOf(500))
                .thenReturn(BigInteger.valueOf(300));

        RedemptionResponse response = redemptionService.requestRedemption(request);
        RedemptionDetailResponse detail = redemptionService.getAdminRedemptionDetail(response.getRedemptionNo());

        System.out.println("=== Redemption detail ===");
        System.out.println(
                "redemptionNo=" + detail.getRedemptionNo()
                        + ", requesterName=" + detail.getRequesterName()
                        + ", requesterType=" + detail.getRequesterType()
                        + ", requesterNo=" + detail.getRequesterNo()
                        + ", account=" + detail.getAccount()
                        + ", walletAddress=" + detail.getWalletAddress()
                        + ", amount=" + detail.getAmount()
                        + ", status=" + detail.getStatus()
                        + ", requestedAt=" + detail.getRequestedAt()
                        + ", processedAt=" + detail.getProcessedAt()
                        + ", cashPaidAt=" + detail.getCashPaidAt()
                        + ", blockNumber=" + detail.getBlockNumber()
                        + ", transactionNo=" + detail.getTransactionNo()
                        + ", txHash=" + detail.getTxHash()
                        + ", failureReason=" + detail.getFailureReason()
        );

        assertThat(detail.getRequesterName()).isEqualTo(beneficiary.getName());
        assertThat(detail.getRequesterType()).isEqualTo(RequesterType.BENEFICIARY.name());
        assertThat(detail.getAccount()).isEqualTo(beneficiary.getAccount());
        assertThat(detail.getWalletAddress()).isEqualTo(beneficiary.getWallet().getWalletAddress());
        assertThat(detail.getAmount()).isEqualTo(300L);
        assertThat(detail.getStatus()).isEqualTo(RedemptionStatus.COMPLETED);
        assertThat(detail.getBlockNumber()).isEqualTo(654L);
        assertThat(detail.getTxHash()).isEqualTo("0xADMIN_DETAIL_HASH");
        assertThat(detail.getTransactionNo()).isNotNull();
    }

    @Test
    @DisplayName("관리자가 입금 완료 처리하면 현금화 상태가 PAID 로 바뀐다")
    void markCashPaid_success() throws Exception {
        Wallet hotWallet = createHotWallet();
        ReflectionTestUtils.setField(redemptionService, "hotWalletAddress", hotWallet.getWalletAddress());

        Foundation foundation = createFoundationWithWallet(new BigDecimal("450"));
        RedemptionRequest request = createRequest(RequesterType.FOUNDATION, foundation.getFoundationNo(), 150L);

        TransactionReceipt receipt = new TransactionReceipt();
        receipt.setTransactionHash("0xPAY_COMPLETE_HASH");
        receipt.setBlockNumber("987");
        receipt.setGasUsed("21000");

        when(blockchainService.redeemOnChain(any(), any(), any())).thenReturn(receipt);
        when(blockchainService.getTokenBalance(any()))
                .thenReturn(BigInteger.valueOf(450))
                .thenReturn(BigInteger.valueOf(300))
                .thenReturn(BigInteger.valueOf(150));

        RedemptionResponse response = redemptionService.requestRedemption(request);
        redemptionService.markCashPaid(response.getRedemptionNo());

        Redemption redemption = redemptionRepository.findById(response.getRedemptionNo()).orElseThrow();
        assertThat(redemption.getStatus()).isEqualTo(RedemptionStatus.PAID);
        assertThat(redemption.getCashPaidAt()).isNotNull();
    }

    @Test
    @DisplayName("이미 입금 완료된 현금화 요청은 다시 입금 완료 처리할 수 없다")
    void markCashPaid_alreadyPaid_fail() throws Exception {
        Wallet hotWallet = createHotWallet();
        ReflectionTestUtils.setField(redemptionService, "hotWalletAddress", hotWallet.getWalletAddress());

        Foundation foundation = createFoundationWithWallet(new BigDecimal("450"));
        RedemptionRequest request = createRequest(RequesterType.FOUNDATION, foundation.getFoundationNo(), 150L);

        TransactionReceipt receipt = new TransactionReceipt();
        receipt.setTransactionHash("0xPAY_DUPLICATE_HASH");
        receipt.setBlockNumber("999");
        receipt.setGasUsed("21000");

        when(blockchainService.redeemOnChain(any(), any(), any())).thenReturn(receipt);
        when(blockchainService.getTokenBalance(any()))
                .thenReturn(BigInteger.valueOf(450))
                .thenReturn(BigInteger.valueOf(300))
                .thenReturn(BigInteger.valueOf(150));

        RedemptionResponse response = redemptionService.requestRedemption(request);
        redemptionService.markCashPaid(response.getRedemptionNo());

        assertThatThrownBy(() -> redemptionService.markCashPaid(response.getRedemptionNo()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("이미 입금 완료 처리된 현금화 요청입니다.");
    }

    @Test
    @DisplayName("현금화 완료 전 상태에서는 입금 완료 처리할 수 없다")
    void markCashPaid_notCompleted_fail() {
        Foundation foundation = createFoundationWithWallet(new BigDecimal("450"));

        Redemption redemption = redemptionRepository.save(
                Redemption.create(
                        RequesterType.FOUNDATION,
                        foundation.getFoundationNo(),
                        150L,
                        foundation.getWallet()
                )
        );

        assertThatThrownBy(() -> redemptionService.markCashPaid(redemption.getRedemptionNo()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("입금 완료 처리는 현금화 완료 건에 대해서만 가능합니다.");
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

    private Foundation createFoundationWithWallet(BigDecimal balance) {
        Wallet requesterWallet = saveWallet(
                WalletType.FOUNDATION,
                "0xFoundationWallet_" + UUID.randomUUID(),
                balance,
                saveKey("foundation-private-key-" + UUID.randomUUID())
        );

        return foundationRepository.save(
                Foundation.builder()
                        .foundationName("테스트재단_" + UUID.randomUUID())
                        .foundationEmail("foundation_" + UUID.randomUUID() + "@test.com")
                        .account("110-123-123456")
                        .wallet(requesterWallet)
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .build()
        );
    }

    private Beneficiary createBeneficiaryWithWallet(BigDecimal balance) {
        Wallet requesterWallet = saveWallet(
                WalletType.BENEFICIARY,
                "0xBeneficiaryWallet_" + UUID.randomUUID(),
                balance,
                saveKey("beneficiary-private-key-" + UUID.randomUUID())
        );

        return beneficiaryRepository.save(
                Beneficiary.builder()
                        .name("테스트수혜자_" + UUID.randomUUID())
                        .email("beneficiary_" + UUID.randomUUID() + "@test.com")
                        .password("1234")
                        .entryCode(1234)
                        .phone("01012345678")
                        .account("3333-12-123456")
                        .beneficiaryType(BeneficiaryType.INDIVIDUAL)
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .wallet(requesterWallet)
                        .build()
        );
    }

    private Redemption findLatestRedemptionByRequesterNo(Long requesterNo) {
        return redemptionRepository.findAll().stream()
                .filter(redemption -> requesterNo.equals(redemption.getRequesterNo()))
                .max(Comparator.comparing(Redemption::getRedemptionNo))
                .orElseThrow();
    }
}

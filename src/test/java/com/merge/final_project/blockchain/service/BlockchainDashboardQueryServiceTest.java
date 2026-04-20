package com.merge.final_project.blockchain.service;

import com.merge.final_project.blockchain.dto.BlockchainTransactionsResponse;
import com.merge.final_project.blockchain.entity.Transaction;
import com.merge.final_project.blockchain.entity.TransactionEventType;
import com.merge.final_project.blockchain.entity.TransactionStatus;
import com.merge.final_project.blockchain.repository.TransactionRepository;
import com.merge.final_project.campaign.campaigns.repository.CampaignRepository;
import com.merge.final_project.org.FoundationRepository;
import com.merge.final_project.recipient.beneficiary.repository.BeneficiaryRepository;
import com.merge.final_project.user.users.UserRepository;
import com.merge.final_project.wallet.repository.WalletLookupRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BlockchainDashboardQueryServiceTest {

    @Mock
    private TransactionRepository transactionRepository;
    @Mock
    private WalletLookupRepository walletLookupRepository;
    @Mock
    private CampaignRepository campaignRepository;
    @Mock
    private FoundationRepository foundationRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private BeneficiaryRepository beneficiaryRepository;

    private BlockchainDashboardQueryService blockchainDashboardQueryService;

    @BeforeEach
    void setUp() {
        blockchainDashboardQueryService = new BlockchainDashboardQueryService(
                transactionRepository,
                walletLookupRepository,
                campaignRepository,
                foundationRepository,
                userRepository,
                beneficiaryRepository,
                new TokenAmountConverter(18)
        );
    }

    @Test
    void getTransactions_convertsGasTopupWeiToNativeCoinAmount() {
        Transaction gasTopup = createTransaction(
                TransactionEventType.POL_AUTO_TOPUP,
                15_000_000_000_000_000L
        );
        when(transactionRepository.findByStatus(eq(TransactionStatus.SUCCESS), any()))
                .thenReturn(new PageImpl<>(List.of(gasTopup), PageRequest.of(0, 10), 1));

        BlockchainTransactionsResponse response = blockchainDashboardQueryService.getTransactions(1, 10, "", "SUCCESS");

        assertThat(response.items()).hasSize(1);
        assertThat(response.items().get(0).eventType()).isEqualTo("GAS_TOPUP");
        assertThat(response.items().get(0).amount()).isEqualByComparingTo("0.015");
    }

    @Test
    void getTransactions_keepsTokenAmountForNonGasEvents() {
        Transaction donation = createTransaction(TransactionEventType.DONATION, 2_500L);
        when(transactionRepository.findByStatus(eq(TransactionStatus.SUCCESS), any()))
                .thenReturn(new PageImpl<>(List.of(donation), PageRequest.of(0, 10), 1));

        BlockchainTransactionsResponse response = blockchainDashboardQueryService.getTransactions(1, 10, "", "SUCCESS");

        assertThat(response.items()).hasSize(1);
        assertThat(response.items().get(0).amount()).isEqualByComparingTo("2500");
    }

    private Transaction createTransaction(TransactionEventType eventType, Long amount) {
        return Transaction.builder()
                .transactionCode("tx-code")
                .txHash("0xhash")
                .blockNum(10L)
                .status(TransactionStatus.SUCCESS)
                .eventType(eventType)
                .amount(amount)
                .gasFee(BigDecimal.ZERO)
                .sentAt(LocalDateTime.of(2026, 4, 17, 12, 0))
                .createdAt(LocalDateTime.of(2026, 4, 17, 12, 0))
                .build();
    }
}

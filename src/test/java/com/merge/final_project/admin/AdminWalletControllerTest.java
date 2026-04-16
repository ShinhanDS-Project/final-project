package com.merge.final_project.admin;

import com.merge.final_project.admin.controller.AdminWalletController;
import com.merge.final_project.admin.dto.AdminTransactionDTO;
import com.merge.final_project.admin.dto.AdminWalletInfoDTO;
import com.merge.final_project.blockchain.entity.Transaction;
import com.merge.final_project.blockchain.entity.TransactionEventType;
import com.merge.final_project.blockchain.entity.TransactionStatus;
import com.merge.final_project.blockchain.repository.TransactionRepository;
import com.merge.final_project.blockchain.wallet.HotWalletResolver;
import com.merge.final_project.wallet.entity.Wallet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminWalletControllerTest {

    @Mock
    private HotWalletResolver hotWalletResolver;

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private AdminWalletController adminWalletController;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(adminWalletController, "configuredHotWalletAddress", "0xHOT");
    }

    @Test
    @DisplayName("HOT wallet info is returned when resolver finds wallet")
    void getHotWalletInfo_success() {
        Wallet hotWallet = mock(Wallet.class);
        when(hotWallet.getWalletAddress()).thenReturn("0xHOT");
        when(hotWallet.getBalance()).thenReturn(new BigDecimal("99999"));
        when(hotWalletResolver.resolve("0xHOT")).thenReturn(hotWallet);

        ResponseEntity<AdminWalletInfoDTO> response = adminWalletController.getHotWalletInfo();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getWalletAddress()).isEqualTo("0xHOT");
        assertThat(response.getBody().getBalance()).isEqualByComparingTo("99999");
    }

    @Test
    @DisplayName("Exception is thrown when resolver cannot find HOT wallet")
    void getHotWalletInfo_notFound() {
        when(hotWalletResolver.resolve("0xHOT")).thenThrow(new IllegalStateException("not found"));
        assertThrows(IllegalStateException.class, () -> adminWalletController.getHotWalletInfo());
    }

    @Test
    @DisplayName("HOT wallet transactions are returned as page")
    void getHotWalletTransactions_success() {
        Wallet fromWallet = mock(Wallet.class);
        when(fromWallet.getWalletAddress()).thenReturn("0xFROM");
        Wallet toWallet = mock(Wallet.class);
        when(toWallet.getWalletAddress()).thenReturn("0xHOT");

        Transaction tx = mock(Transaction.class);
        when(tx.getTransactionNo()).thenReturn(1L);
        when(tx.getFromWallet()).thenReturn(fromWallet);
        when(tx.getToWallet()).thenReturn(toWallet);
        when(tx.getAmount()).thenReturn(1000L);
        when(tx.getEventType()).thenReturn(TransactionEventType.REDEMPTION);
        when(tx.getStatus()).thenReturn(TransactionStatus.SUCCESS);
        when(tx.getSentAt()).thenReturn(LocalDateTime.now());

        Wallet hotWallet = mock(Wallet.class);
        when(hotWallet.getWalletAddress()).thenReturn("0xHOT");
        when(hotWalletResolver.resolve("0xHOT")).thenReturn(hotWallet);

        Page<Transaction> txPage = new PageImpl<>(List.of(tx));
        when(transactionRepository.findByWalletAddressPaged(eq("0xHOT"), any()))
                .thenReturn(txPage);

        ResponseEntity<Page<AdminTransactionDTO>> response =
                adminWalletController.getHotWalletTransactions(PageRequest.of(0, 10));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getContent()).hasSize(1);
        assertThat(response.getBody().getContent().get(0).getToWalletAddress()).isEqualTo("0xHOT");
    }

    @Test
    @DisplayName("Empty page is returned when no HOT wallet transactions")
    void getHotWalletTransactions_empty() {
        Wallet hotWallet = mock(Wallet.class);
        when(hotWallet.getWalletAddress()).thenReturn("0xHOT");
        when(hotWalletResolver.resolve("0xHOT")).thenReturn(hotWallet);
        when(transactionRepository.findByWalletAddressPaged(eq("0xHOT"), any()))
                .thenReturn(Page.empty());

        ResponseEntity<Page<AdminTransactionDTO>> response =
                adminWalletController.getHotWalletTransactions(PageRequest.of(0, 10));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getContent()).isEmpty();
    }

    @Test
    @DisplayName("Exception is thrown for transaction query when resolver fails")
    void getHotWalletTransactions_notFound() {
        when(hotWalletResolver.resolve("0xHOT")).thenThrow(new IllegalStateException("not found"));
        assertThrows(IllegalStateException.class,
                () -> adminWalletController.getHotWalletTransactions(PageRequest.of(0, 10)));
    }
}

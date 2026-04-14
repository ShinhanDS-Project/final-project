package com.merge.final_project.admin;

import com.merge.final_project.admin.controller.AdminWalletController;
import com.merge.final_project.admin.dto.AdminTransactionDTO;
import com.merge.final_project.admin.dto.AdminWalletInfoDTO;
import com.merge.final_project.blockchain.entity.Transaction;
import com.merge.final_project.blockchain.entity.TransactionEventType;
import com.merge.final_project.blockchain.entity.TransactionStatus;
import com.merge.final_project.blockchain.repository.TransactionRepository;
import com.merge.final_project.wallet.entity.Wallet;
import com.merge.final_project.wallet.entity.WalletType;
import com.merge.final_project.wallet.repository.WalletLookupRepository;
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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminWalletControllerTest {

    @Mock
    private WalletLookupRepository walletLookupRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private AdminWalletController adminWalletController;

    // ─── HOT 지갑 정보 조회 ───

    @Test
    @DisplayName("HOT 지갑이 존재하면 주소와 잔액을 반환한다")
    void HOT지갑_정상조회() {
        Wallet hotWallet = mock(Wallet.class);
        when(hotWallet.getWalletAddress()).thenReturn("0xHOT");
        when(hotWallet.getBalance()).thenReturn(new BigDecimal("99999"));
        when(walletLookupRepository.findFirstByWalletType(WalletType.HOT))
                .thenReturn(Optional.of(hotWallet));

        ResponseEntity<AdminWalletInfoDTO> response = adminWalletController.getHotWalletInfo();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getWalletAddress()).isEqualTo("0xHOT");
        assertThat(response.getBody().getBalance()).isEqualByComparingTo("99999");
    }

    @Test
    @DisplayName("HOT 지갑이 없으면 IllegalStateException이 발생한다")
    void HOT지갑_없으면_예외() {
        when(walletLookupRepository.findFirstByWalletType(WalletType.HOT))
                .thenReturn(Optional.empty());

        assertThrows(IllegalStateException.class, () -> adminWalletController.getHotWalletInfo());
    }

    // ─── HOT 지갑 거래 내역 조회 ───

    @Test
    @DisplayName("HOT 지갑 거래 내역을 페이징으로 반환한다")
    void HOT지갑_거래내역_정상조회() {
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
        when(walletLookupRepository.findFirstByWalletType(WalletType.HOT))
                .thenReturn(Optional.of(hotWallet));

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
    @DisplayName("HOT 지갑 거래 내역이 없으면 빈 페이지를 반환한다")
    void HOT지갑_거래내역_없으면_빈페이지() {
        Wallet hotWallet = mock(Wallet.class);
        when(hotWallet.getWalletAddress()).thenReturn("0xHOT");
        when(walletLookupRepository.findFirstByWalletType(WalletType.HOT))
                .thenReturn(Optional.of(hotWallet));
        when(transactionRepository.findByWalletAddressPaged(eq("0xHOT"), any()))
                .thenReturn(Page.empty());

        ResponseEntity<Page<AdminTransactionDTO>> response =
                adminWalletController.getHotWalletTransactions(PageRequest.of(0, 10));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getContent()).isEmpty();
    }

    @Test
    @DisplayName("거래 내역 조회 시 HOT 지갑이 없으면 IllegalStateException이 발생한다")
    void 거래내역조회_HOT지갑없으면_예외() {
        when(walletLookupRepository.findFirstByWalletType(WalletType.HOT))
                .thenReturn(Optional.empty());

        assertThrows(IllegalStateException.class,
                () -> adminWalletController.getHotWalletTransactions(PageRequest.of(0, 10)));
    }
}

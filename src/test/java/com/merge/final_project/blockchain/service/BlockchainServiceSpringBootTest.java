package com.merge.final_project.blockchain.service;

import com.merge.final_project.blockchain.tx.BlockchainTransferClient;
import com.merge.final_project.blockchain.tx.TransferResult;
import com.merge.final_project.db.entity.Campaign;
import com.merge.final_project.db.entity.KeyEntity;
import com.merge.final_project.db.entity.TokenTransaction;
import com.merge.final_project.db.entity.Wallet;
import com.merge.final_project.db.entity.id.WalletId;
import com.merge.final_project.db.repository.CampaignRepository;
import com.merge.final_project.db.repository.KeyEntityRepository;
import com.merge.final_project.db.repository.TokenTransactionRepository;
import com.merge.final_project.db.repository.WalletRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigInteger;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest(
        classes = {BlockchainService.class, TransactionService.class, EventService.class},
        properties = {
                "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration,org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration",
                "blockchain.token.decimals=18",
                "blockchain.contract.owner-address=0x68a498a44468cB991Cde892dcDE9e073D706BA6D"
        }
)
class BlockchainServiceSpringBootTest {

    @Autowired
    private BlockchainService blockchainService;

    @MockitoBean
    private WalletRepository walletRepository;

    @MockitoBean
    private CampaignRepository campaignRepository;

    @MockitoBean
    private TokenTransactionRepository tokenTransactionRepository;

    @MockitoBean
    private KeyEntityRepository keyEntityRepository;

    @MockitoBean
    private BlockchainTransferClient blockchainTransferClient;

    @MockitoBean
    private com.merge.final_project.blockchain.security.WalletCryptoService walletCryptoService;

    @Test
    @DisplayName("결제 완료 시 TokenAllocated 이벤트 값이 token_transaction에 반영된다")
    void chargeUserToken_records_transaction_with_event_data() {
        Wallet hot = walletWithId(1L, 1L, "SERVER", "HOT", "0xHot");
        Wallet owner = walletWithId(2L, 2L, "SERVER", "OWNER", "0x68a498a44468cB991Cde892dcDE9e073D706BA6D");
        Wallet user = walletWithId(3L, 3L, "USER", "101", "0xUser101");

        when(walletRepository.findByWalletTypeAndOwnerNo("SERVER", "HOT")).thenReturn(Optional.of(hot));
        when(walletRepository.findByWalletTypeAndOwnerNo("USER", "101")).thenReturn(Optional.of(user));
        when(walletRepository.findByWalletAddressIgnoreCase("0x68a498a44468cB991Cde892dcDE9e073D706BA6D"))
                .thenReturn(Optional.of(owner));
        when(walletRepository.findByWalletAddressIgnoreCase("0xUser101")).thenReturn(Optional.of(user));
        when(keyEntityRepository.findById(2L)).thenReturn(Optional.of(keyWith(2L, "enc-owner-private-key")));
        when(walletCryptoService.decryptPrivateKey("enc-owner-private-key")).thenReturn("owner-private-key");
        when(tokenTransactionRepository.save(any(TokenTransaction.class))).thenAnswer(invocation -> invocation.getArgument(0));

        BigInteger onChainAmount = BigInteger.valueOf(100L).multiply(BigInteger.TEN.pow(18));
        when(blockchainTransferClient.allocateToUser(
                eq("owner-private-key"),
                eq("0xUser101"),
                eq(onChainAmount),
                eq(BigInteger.valueOf(999L))
        )).thenReturn(new TransferResult(
                "0xabc",
                12345L,
                "SUCCESS",
                "allocateToUser",
                "TokenAllocated",
                null,
                "0xUser101",
                BigInteger.valueOf(999L),
                null,
                onChainAmount
        ));

        var response = blockchainService.chargeUserToken(101, 100L, 999L);

        assertThat(response.status()).isEqualTo("SUCCESS");
        assertThat(response.txHash()).isEqualTo("0xabc");

        ArgumentCaptor<TokenTransaction> captor = ArgumentCaptor.forClass(TokenTransaction.class);
        verify(tokenTransactionRepository, atLeastOnce()).save(captor.capture());
        TokenTransaction lastSaved = captor.getAllValues().get(captor.getAllValues().size() - 1);
        assertThat(lastSaved.getEventType()).isEqualTo("TokenAllocated");
        assertThat(lastSaved.getToWalletNo()).isEqualTo(3);
        assertThat(lastSaved.getAmount()).isEqualTo(100);
    }

    @Test
    @DisplayName("기부 전송 시 DonationSent 이벤트 값이 token_transaction에 반영된다")
    void donateToCampaign_records_transaction_with_event_data() {
        Wallet user = walletWithId(10L, 10L, "USER", "201", "0xUser201");
        Wallet campaignWallet = walletWithId(20L, 20L, "CAMPAIGN", "1:CAMPAIGN:1", "0xCampaign1");

        Campaign campaign = new Campaign();
        campaign.setCampaignNo(777);
        campaign.setWalletNo("20");

        when(walletRepository.findByWalletTypeAndOwnerNo("USER", "201")).thenReturn(Optional.of(user));
        when(campaignRepository.findById(777)).thenReturn(Optional.of(campaign));
        when(walletRepository.findByIdWalletNo(20L)).thenReturn(Optional.of(campaignWallet));
        when(walletRepository.findByWalletAddressIgnoreCase("0xUser201")).thenReturn(Optional.of(user));
        when(walletRepository.findByWalletAddressIgnoreCase("0xCampaign1")).thenReturn(Optional.of(campaignWallet));
        when(keyEntityRepository.findById(10L)).thenReturn(Optional.of(keyWith(10L, "enc-user-private-key")));
        when(walletCryptoService.decryptPrivateKey("enc-user-private-key")).thenReturn("user-private-key");
        when(tokenTransactionRepository.save(any(TokenTransaction.class))).thenAnswer(invocation -> invocation.getArgument(0));

        BigInteger onChainAmount = BigInteger.valueOf(55L).multiply(BigInteger.TEN.pow(18));
        when(blockchainTransferClient.donateToCampaign(
                eq("user-private-key"),
                eq("0xCampaign1"),
                eq(onChainAmount),
                eq(BigInteger.valueOf(777L)),
                eq(BigInteger.valueOf(333L))
        )).thenReturn(new TransferResult(
                "0xdef",
                45678L,
                "SUCCESS",
                "donateToCampaign",
                "DonationSent",
                "0xUser201",
                "0xCampaign1",
                BigInteger.valueOf(333L),
                BigInteger.valueOf(777L),
                onChainAmount
        ));

        var response = blockchainService.transferDonationToCampaign(201, 777L, 55L, 333L);

        assertThat(response.status()).isEqualTo("SUCCESS");
        assertThat(response.txHash()).isEqualTo("0xdef");

        ArgumentCaptor<TokenTransaction> captor = ArgumentCaptor.forClass(TokenTransaction.class);
        verify(tokenTransactionRepository, atLeastOnce()).save(captor.capture());
        TokenTransaction lastSaved = captor.getAllValues().get(captor.getAllValues().size() - 1);
        assertThat(lastSaved.getEventType()).isEqualTo("DonationSent");
        assertThat(lastSaved.getFromWalletNo()).isEqualTo(10);
        assertThat(lastSaved.getToWalletNo()).isEqualTo(20);
        assertThat(lastSaved.getAmount()).isEqualTo(55);
    }

    private Wallet walletWithId(Long walletNo, Long keyNo, String walletType, String ownerNo, String address) {
        Wallet wallet = new Wallet();
        WalletId id = new WalletId();
        id.setWalletNo(walletNo);
        id.setKeyNo(keyNo);
        wallet.setId(id);
        wallet.setWalletType(walletType);
        wallet.setOwnerNo(ownerNo);
        wallet.setWalletAddress(address);
        wallet.setStatus("ACTIVE");
        return wallet;
    }

    private KeyEntity keyWith(Long keyNo, String privateKey) {
        KeyEntity key = new KeyEntity();
        key.setKeyNo(keyNo);
        key.setPrivateKey(privateKey);
        return key;
    }
}

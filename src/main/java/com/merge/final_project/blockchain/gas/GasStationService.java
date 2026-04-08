package com.merge.final_project.blockchain.gas;

import com.merge.final_project.blockchain.entity.Key;
import com.merge.final_project.blockchain.repository.KeyRepository;
import com.merge.final_project.blockchain.security.WalletCryptoService;
import com.merge.final_project.blockchain.service.TransferTransactionService;
import com.merge.final_project.blockchain.tx.BlockchainTransferClient;
import com.merge.final_project.blockchain.tx.TransferResult;
import com.merge.final_project.wallet.entity.Wallet;
import com.merge.final_project.wallet.entity.WalletType;
import com.merge.final_project.wallet.repository.WalletLookupRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;

import java.math.BigInteger;

@Slf4j
@Service
public class GasStationService {

    private final WalletLookupRepository walletLookupRepository;
    private final KeyRepository keyRepository;
    private final BlockchainTransferClient blockchainTransferClient;
    private final TransferTransactionService transferTransactionService;
    private final WalletCryptoService walletCryptoService;
    private final Web3j web3j;
    private final BigInteger initialPolWei;
    private final BigInteger minPolWei;

    public GasStationService(WalletLookupRepository walletLookupRepository,
                             KeyRepository keyRepository,
                             BlockchainTransferClient blockchainTransferClient,
                             TransferTransactionService transferTransactionService,
                             WalletCryptoService walletCryptoService,
                             Web3j web3j,
                             @Value("${blockchain.gas.initial-pol-wei:30000000000000000}") String initialPolWei,
                             @Value("${blockchain.gas.min-pol-wei:10000000000000000}") String minPolWei) {
        this.walletLookupRepository = walletLookupRepository;
        this.keyRepository = keyRepository;
        this.blockchainTransferClient = blockchainTransferClient;
        this.transferTransactionService = transferTransactionService;
        this.walletCryptoService = walletCryptoService;
        this.web3j = web3j;
        this.initialPolWei = new BigInteger(initialPolWei);
        this.minPolWei = new BigInteger(minPolWei);
    }

    @Transactional
    public void fundInitialPol(Wallet wallet) {
        topUpPolFromHot(wallet, initialPolWei, "ALLOCATION");
    }

    @Transactional
    public void ensureSufficientPol(Wallet signerWallet) {
        if (signerWallet == null || signerWallet.getWalletAddress() == null || signerWallet.getWalletAddress().isBlank()) {
            return;
        }
        if (isHotWallet(signerWallet)) {
            return;
        }
        BigInteger currentBalance;
        try {
            currentBalance = web3j.ethGetBalance(signerWallet.getWalletAddress(), DefaultBlockParameterName.LATEST)
                    .send()
                    .getBalance();
        } catch (Exception e) {
            log.warn("skip gas precheck: failed to read native balance. walletNo={}, address={}, reason={}",
                    signerWallet.getWalletNo(), signerWallet.getWalletAddress(), e.getMessage());
            return;
        }
        if (currentBalance.compareTo(minPolWei) >= 0) {
            return;
        }
        topUpPolFromHot(signerWallet, initialPolWei, "ALLOCATION");
    }

    private void topUpPolFromHot(Wallet wallet, BigInteger amountWei, String eventType) {
        if (wallet == null || isHotWallet(wallet)) {
            return;
        }

        Wallet hotWallet = walletLookupRepository.findFirstByWalletType(WalletType.HOT)
                .orElse(null);
        if (hotWallet == null) {
            log.warn("skip POL top-up: HOT wallet not found");
            return;
        }

        TransferResult transferResult = blockchainTransferClient.transferNative(
                hotWallet.getWalletAddress(),
                resolveWalletPrivateKey(hotWallet),
                wallet.getWalletAddress(),
                amountWei
        );

        transferTransactionService.saveTransfer(
                hotWallet,
                wallet,
                amountWei.longValue(),
                transferResult.txHash(),
                transferResult.blockNumber(),
                transferResult.status(),
                eventType
        );
        if (!"SUCCESS".equalsIgnoreCase(transferResult.status())) {
            log.warn("POL funding failed. eventType={}, walletNo={}, message={}",
                    eventType, wallet.getWalletNo(), transferResult.message());
            throw new IllegalStateException("POL top-up failed: " + transferResult.message());
        }
        log.info("POL funding completed. eventType={}, walletNo={}, txHash={}",
                eventType, wallet.getWalletNo(), transferResult.txHash());
    }

    private boolean isHotWallet(Wallet wallet) {
        return wallet != null && wallet.getWalletType() == WalletType.HOT;
    }

    private String resolveWalletPrivateKey(Wallet wallet) {
        if (wallet.getKey() == null || wallet.getKey().getKeyNo() == null) {
            throw new IllegalStateException("wallet key reference is missing. walletNo=" + wallet.getWalletNo());
        }

        Key key = keyRepository.findById(wallet.getKey().getKeyNo())
                .orElseThrow(() -> new IllegalStateException("key row not found: " + wallet.getKey().getKeyNo()));
        String storedPrivateKey = key.getPrivateKey();
        if (storedPrivateKey == null || storedPrivateKey.isBlank()) {
            throw new IllegalStateException("private key is empty. keyNo=" + key.getKeyNo());
        }

        try {
            return walletCryptoService.decryptPrivateKey(storedPrivateKey);
        } catch (RuntimeException e) {
            log.warn("keyNo={} is not decryptable payload. fallback to raw key string.", key.getKeyNo());
            return storedPrivateKey;
        }
    }
}

package com.merge.final_project.blockchain.gas;

import com.merge.final_project.blockchain.security.WalletCryptoService;
import com.merge.final_project.blockchain.service.TransactionService;
import com.merge.final_project.blockchain.tx.BlockchainTransferClient;
import com.merge.final_project.blockchain.tx.TransferResult;
import com.merge.final_project.db.entity.KeyEntity;
import com.merge.final_project.db.entity.Wallet;
import com.merge.final_project.db.repository.KeyEntityRepository;
import com.merge.final_project.db.repository.WalletRepository;
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

    private static final String WALLET_TYPE_SERVER = "SERVER";
    private static final String SERVER_OWNER_HOT = "HOT";

    private final WalletRepository walletRepository;
    private final KeyEntityRepository keyEntityRepository;
    private final BlockchainTransferClient blockchainTransferClient;
    private final TransactionService transactionService;
    private final WalletCryptoService walletCryptoService;
    private final Web3j web3j;
    private final BigInteger initialPolWei;
    private final BigInteger minPolWei;

    public GasStationService(WalletRepository walletRepository,
                             KeyEntityRepository keyEntityRepository,
                             BlockchainTransferClient blockchainTransferClient,
                             TransactionService transactionService,
                             WalletCryptoService walletCryptoService,
                             Web3j web3j,
                             @Value("${blockchain.gas.initial-pol-wei:30000000000000000}") String initialPolWei,
                             @Value("${blockchain.gas.min-pol-wei:10000000000000000}") String minPolWei) {
        this.walletRepository = walletRepository;
        this.keyEntityRepository = keyEntityRepository;
        this.blockchainTransferClient = blockchainTransferClient;
        this.transactionService = transactionService;
        this.walletCryptoService = walletCryptoService;
        this.web3j = web3j;
        this.initialPolWei = new BigInteger(initialPolWei);
        this.minPolWei = new BigInteger(minPolWei);
    }

    /**
     * 신규 지갑(서버 지갑 제외)에 초기 POL 가스를 지급한다.
     * 출금 지갑은 SERVER/HOT 지갑을 사용한다.
     */
    @Transactional
    public void fundInitialPol(Wallet wallet) {
        topUpPolFromHot(wallet, initialPolWei, "POL_TOPUP");
    }

    /**
     * 서명 지갑의 네이티브 가스 잔액이 임계치 미만이면 HOT 지갑에서 자동 충전한다.
     */
    @Transactional
    public void ensureSufficientPol(Wallet signerWallet) {
        if (signerWallet == null || signerWallet.getWalletAddress() == null || signerWallet.getWalletAddress().isBlank()) {
            return;
        }
        if (isServerHotWallet(signerWallet)) {
            return;
        }
        BigInteger currentBalance;
        try {
            currentBalance = web3j.ethGetBalance(signerWallet.getWalletAddress(), DefaultBlockParameterName.LATEST)
                    .send()
                    .getBalance();
        } catch (Exception e) {
            Long walletNo = signerWallet.getId() == null ? null : signerWallet.getId().getWalletNo();
            log.warn("skip gas precheck: failed to read native balance. walletNo={}, address={}, reason={}",
                    walletNo, signerWallet.getWalletAddress(), e.getMessage());
            return;
        }
        if (currentBalance.compareTo(minPolWei) >= 0) {
            return;
        }
        topUpPolFromHot(signerWallet, initialPolWei, "POL_AUTO_TOPUP");
    }

    private void topUpPolFromHot(Wallet wallet, BigInteger amountWei, String eventType) {
        if (wallet == null) {
            return;
        }
        if (isServerHotWallet(wallet)) {
            return;
        }

        Wallet hotWallet = walletRepository.findByWalletTypeAndOwnerNo(WALLET_TYPE_SERVER, SERVER_OWNER_HOT)
                .orElse(null);
        if (hotWallet == null) {
            log.warn("skip POL top-up: server HOT wallet not found");
            return;
        }

        TransferResult transferResult = blockchainTransferClient.transferNative(
                hotWallet.getWalletAddress(),
                resolveWalletPrivateKey(hotWallet),
                wallet.getWalletAddress(),
                amountWei
        );

        transactionService.saveTransfer(
                hotWallet,
                wallet,
                amountWei.longValue(),
                transferResult.txHash(),
                transferResult.blockNumber(),
                transferResult.status(),
                eventType
        );
        Long walletNo = wallet.getId() == null ? null : wallet.getId().getWalletNo();
        if (!"SUCCESS".equalsIgnoreCase(transferResult.status())) {
            log.warn("POL funding failed. eventType={}, walletNo={}, message={}",
                    eventType, walletNo, transferResult.message());
            throw new IllegalStateException("POL top-up failed: " + transferResult.message());
        }
        log.info("POL funding completed. eventType={}, walletNo={}, txHash={}", eventType, walletNo, transferResult.txHash());
    }

    private boolean isServerHotWallet(Wallet wallet) {
        if (wallet == null) {
            return false;
        }
        return WALLET_TYPE_SERVER.equalsIgnoreCase(wallet.getWalletType())
                && SERVER_OWNER_HOT.equalsIgnoreCase(wallet.getOwnerNo());
    }

    private String resolveWalletPrivateKey(Wallet wallet) {
        if (wallet.getId() == null || wallet.getId().getKeyNo() == null) {
            throw new IllegalStateException("wallet key reference is missing. walletAddress=" + wallet.getWalletAddress());
        }

        KeyEntity keyEntity = keyEntityRepository.findById(wallet.getId().getKeyNo())
                .orElseThrow(() -> new IllegalStateException("key row not found: " + wallet.getId().getKeyNo()));
        String storedPrivateKey = keyEntity.getPrivateKey();
        if (storedPrivateKey == null || storedPrivateKey.isBlank()) {
            throw new IllegalStateException("private key is empty. keyNo=" + keyEntity.getKeyNo());
        }

        try {
            return walletCryptoService.decryptPrivateKey(storedPrivateKey);
        } catch (RuntimeException e) {
            log.warn("keyNo={} is not decryptable payload. fallback to raw key string.", keyEntity.getKeyNo());
            return storedPrivateKey;
        }
    }
}

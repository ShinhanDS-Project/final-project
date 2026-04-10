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

    /**
     * 신규 ACTIVE 지갑 생성 직후 1회 초기 POL 가스비를 충전한다.
     */
    @Transactional
    public void fundInitialPol(Wallet wallet) {
        topUpPolFromHot(wallet, initialPolWei, "POL_AUTO_TOPUP");
    }

    /**
     * 온체인 서명 직전 가스비 상태를 점검한다.
     * 잔액이 임계치보다 낮으면 HOT 지갑에서 자동 충전한다.
     */
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
        topUpPolFromHot(signerWallet, initialPolWei, "POL_AUTO_TOPUP");
    }

    /**
     * HOT 지갑에서 대상 지갑으로 네이티브 코인을 전송하고 거래내역을 저장한다.
     * 가스 충전에 실패하면 후속 트랜잭션을 안전하게 진행할 수 없으므로 예외를 던진다.
     */
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

    /**
     * HOT 지갑은 가스 공급원이며 자기 자신을 다시 충전하지 않는다.
     */
    private boolean isHotWallet(Wallet wallet) {
        return wallet != null && wallet.getWalletType() == WalletType.HOT;
    }

    /**
     * key 테이블에서 서명 지갑 키를 읽어 복호화한다.
     * 레거시 평문 키와의 호환을 위해 복호화 실패 시 원문을 그대로 사용한다.
     */
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

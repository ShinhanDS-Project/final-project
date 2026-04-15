package com.merge.final_project.blockchain.gas;

import com.merge.final_project.blockchain.security.WalletPrivateKeyResolver;
import com.merge.final_project.blockchain.service.TransferTransactionService;
import com.merge.final_project.blockchain.tx.BlockchainTransferClient;
import com.merge.final_project.blockchain.tx.TransferResult;
import com.merge.final_project.blockchain.wallet.HotWalletResolver;
import com.merge.final_project.wallet.entity.Wallet;
import com.merge.final_project.wallet.entity.WalletType;
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

    private final HotWalletResolver hotWalletResolver;
    private final BlockchainTransferClient blockchainTransferClient;
    private final TransferTransactionService transferTransactionService;
    private final WalletPrivateKeyResolver walletPrivateKeyResolver;
    private final Web3j web3j;
    private final BigInteger initialPolWei;
    private final BigInteger minPolWei;
    private final BigInteger maxTopUpPolWei;
    private final BigInteger txGasLimit;
    private final int topUpBufferBps;
    private final BigInteger topUpHeadroomWei;
    private final int dynamicCapMultiplier;
    private final String hotWalletAddress;

    public GasStationService(
            HotWalletResolver hotWalletResolver,
            BlockchainTransferClient blockchainTransferClient,
            TransferTransactionService transferTransactionService,
            WalletPrivateKeyResolver walletPrivateKeyResolver,
            Web3j web3j,
            @Value("${blockchain.gas.initial-pol-wei:15000000000000000}") String initialPolWei,
            @Value("${blockchain.gas.min-pol-wei:10000000000000000}") String minPolWei,
            @Value("${blockchain.gas.max-topup-pol-wei:50000000000000000}") String maxTopUpPolWei,
            @Value("${blockchain.tx.gas-limit:120000}") String txGasLimit,
            @Value("${blockchain.gas.topup-buffer-bps:12500}") String topUpBufferBps,
            @Value("${blockchain.gas.topup-headroom-wei:5000000000000000}") String topUpHeadroomWei,
            @Value("${blockchain.gas.dynamic-cap-multiplier:3}") String dynamicCapMultiplier,
            @Value("${blockchain.wallet.hot-address:}") String hotWalletAddress
    ) {
        this.hotWalletResolver = hotWalletResolver;
        this.blockchainTransferClient = blockchainTransferClient;
        this.transferTransactionService = transferTransactionService;
        this.walletPrivateKeyResolver = walletPrivateKeyResolver;
        this.web3j = web3j;
        this.initialPolWei = new BigInteger(initialPolWei);
        this.minPolWei = new BigInteger(minPolWei);
        this.maxTopUpPolWei = new BigInteger(maxTopUpPolWei);
        this.txGasLimit = new BigInteger(txGasLimit);
        this.topUpBufferBps = Integer.parseInt(topUpBufferBps);
        this.topUpHeadroomWei = new BigInteger(topUpHeadroomWei);
        this.dynamicCapMultiplier = Integer.parseInt(dynamicCapMultiplier);
        this.hotWalletAddress = hotWalletAddress;
    }

    @Transactional
    public void fundInitialPol(Wallet wallet) {
        // 초기 가스 지급도 동적 기준을 적용해 가스 급등 시 과소 충전을 방지한다.
        BigInteger amountWei = resolveInitialTopUpAmount(BigInteger.ZERO);
        topUpPolFromHot(wallet, amountWei, "POL_AUTO_TOPUP");
    }

    @Transactional
    public void ensureSufficientPol(Wallet signerWallet) {
        // HOT 지갑 자체와 비정상 입력은 제외하고, 실제 서명 지갑만 점검한다.
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
            log.warn(
                    "skip gas precheck: failed to read native balance. walletNo={}, address={}, reason={}",
                    signerWallet.getWalletNo(),
                    signerWallet.getWalletAddress(),
                    e.getMessage()
            );
            return;
        }

        // 조기 반환 기준도 고정 minPol가 아니라 동적 필요 예비금 기준으로 판정한다.
        BigInteger requiredReserveWei = resolveRequiredGasReserveWei();
        if (currentBalance.compareTo(requiredReserveWei) >= 0) {
            return;
        }

        // 동적 목표치 대비 부족분만 계산해서 충전한다.
        BigInteger amountWei = resolveEnsureTopUpAmount(currentBalance, requiredReserveWei);
        topUpPolFromHot(signerWallet, amountWei, "POL_AUTO_TOPUP");
    }

    private void topUpPolFromHot(Wallet receiverWallet, BigInteger amountWei, String eventType) {
        if (receiverWallet == null || isHotWallet(receiverWallet)) {
            return;
        }
        BigInteger dynamicCapWei = resolveDynamicTopUpCapWei();
        validateTopUpAmount(amountWei, eventType, receiverWallet, dynamicCapWei);

        Wallet hotWallet = resolveHotWallet();
        TransferResult transferResult = blockchainTransferClient.transferNative(
                hotWallet.getWalletAddress(),
                walletPrivateKeyResolver.resolveForWallet(hotWallet),
                receiverWallet.getWalletAddress(),
                amountWei
        );

        transferTransactionService.saveTransfer(
                hotWallet,
                receiverWallet,
                toRecordedAmount(amountWei),
                transferResult.txHash(),
                transferResult.blockNumber(),
                transferResult.status(),
                eventType
        );
        if (!"SUCCESS".equalsIgnoreCase(transferResult.status())) {
            log.warn(
                    "POL funding failed. eventType={}, walletNo={}, message={}",
                    eventType,
                    receiverWallet.getWalletNo(),
                    transferResult.message()
            );
            throw new IllegalStateException("POL top-up failed: " + transferResult.message());
        }
        log.info(
                "POL funding completed. eventType={}, walletNo={}, txHash={}",
                eventType,
                receiverWallet.getWalletNo(),
                transferResult.txHash()
        );
    }

    private void validateTopUpAmount(BigInteger amountWei, String eventType, Wallet receiverWallet, BigInteger dynamicCapWei) {
        if (amountWei == null || amountWei.signum() <= 0) {
            throw new IllegalArgumentException("invalid POL top-up amount. walletNo=" + receiverWallet.getWalletNo());
        }
        if (amountWei.compareTo(dynamicCapWei) > 0) {
            throw new IllegalStateException(
                    "POL top-up amount exceeds dynamic safety limit. eventType=" + eventType
                            + ", walletNo=" + receiverWallet.getWalletNo()
                            + ", amountWei=" + amountWei
                            + ", maxWei=" + dynamicCapWei
            );
        }
    }

    private BigInteger resolveInitialTopUpAmount(BigInteger currentBalanceWei) {
        // targetWei = max(초기 기준값, 동적 필요 예비금)
        BigInteger safeCurrentBalance = currentBalanceWei == null ? BigInteger.ZERO : currentBalanceWei;
        BigInteger requiredWei = resolveRequiredGasReserveWei();
        BigInteger targetWei = initialPolWei.max(requiredWei);
        BigInteger shortageWei = targetWei.subtract(safeCurrentBalance);
        if (shortageWei.signum() <= 0) {
            return BigInteger.ZERO;
        }
        BigInteger dynamicCapWei = resolveDynamicTopUpCapWei();
        if (shortageWei.compareTo(dynamicCapWei) > 0) {
            log.warn(
                    "POL top-up shortage exceeds dynamic cap. shortageWei={}, capWei={}, targetWei={}, currentWei={}",
                    shortageWei,
                    dynamicCapWei,
                    targetWei,
                    safeCurrentBalance
            );
            return dynamicCapWei;
        }
        return shortageWei;
    }

    private BigInteger resolveEnsureTopUpAmount(BigInteger currentBalanceWei, BigInteger requiredReserveWei) {
        BigInteger safeCurrentBalance = currentBalanceWei == null ? BigInteger.ZERO : currentBalanceWei;
        BigInteger targetWei = minPolWei.max(requiredReserveWei);
        BigInteger shortageWei = targetWei.subtract(safeCurrentBalance);
        if (shortageWei.signum() <= 0) {
            return BigInteger.ZERO;
        }
        BigInteger dynamicCapWei = resolveDynamicTopUpCapWei();
        if (shortageWei.compareTo(dynamicCapWei) > 0) {
            log.warn(
                    "POL top-up shortage exceeds dynamic cap. shortageWei={}, capWei={}, targetWei={}, currentWei={}",
                    shortageWei,
                    dynamicCapWei,
                    targetWei,
                    safeCurrentBalance
            );
            return dynamicCapWei;
        }
        return shortageWei;
    }

    private BigInteger resolveRequiredGasReserveWei() {
        // requiredReserve = max(minPol, gasPrice * gasLimit * buffer + headroom)
        // gasPrice를 실시간으로 조회해 현재 네트워크 상황을 반영한다.
        BigInteger gasPriceWei = fetchGasPriceOrFallback();
        BigInteger baseRequiredWei = gasPriceWei.multiply(txGasLimit);
        BigInteger numerator = baseRequiredWei.multiply(BigInteger.valueOf(topUpBufferBps));
        BigInteger denominator = BigInteger.valueOf(10_000);
        BigInteger bufferedRequiredWei = numerator.add(denominator.subtract(BigInteger.ONE)).divide(denominator);
        return minPolWei.max(bufferedRequiredWei.add(topUpHeadroomWei));
    }

    private BigInteger resolveDynamicTopUpCapWei() {
        // 상한도 동적으로 확장해 가스 급등 시 고정 상한 오탐을 줄인다.
        BigInteger requiredWei = resolveRequiredGasReserveWei();
        BigInteger dynamicWei = requiredWei.multiply(BigInteger.valueOf(dynamicCapMultiplier));
        return maxTopUpPolWei.max(dynamicWei);
    }

    private BigInteger fetchGasPriceOrFallback() {
        try {
            BigInteger gasPriceWei = web3j.ethGasPrice().send().getGasPrice();
            if (gasPriceWei == null || gasPriceWei.signum() <= 0) {
                return BigInteger.ZERO;
            }
            return gasPriceWei;
        } catch (Exception e) {
            log.warn("failed to fetch gasPrice for dynamic top-up. fallback to configured minimum reserve.", e);
            return BigInteger.ZERO;
        }
    }

    private Wallet resolveHotWallet() {
        return hotWalletResolver.resolve(hotWalletAddress);
    }

    private boolean isHotWallet(Wallet wallet) {
        return wallet != null && wallet.getWalletType() == WalletType.HOT;
    }

    private Long toRecordedAmount(BigInteger amountWei) {
        try {
            return amountWei.longValueExact();
        } catch (ArithmeticException e) {
            throw new IllegalStateException("POL top-up amount exceeds token_transaction.amount range: " + amountWei, e);
        }
    }
}

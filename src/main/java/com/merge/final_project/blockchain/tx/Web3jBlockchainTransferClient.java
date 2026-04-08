package com.merge.final_project.blockchain.tx;

import com.merge.final_project.blockchain.generated.GiveNToken;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.EthGetTransactionReceipt;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tx.RawTransactionManager;
import org.web3j.tx.TransactionManager;
import org.web3j.tx.gas.StaticGasProvider;
import org.web3j.tx.response.PollingTransactionReceiptProcessor;
import org.web3j.tx.response.TransactionReceiptProcessor;

import java.math.BigInteger;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "blockchain.stub.enabled", havingValue = "false", matchIfMissing = true)
public class Web3jBlockchainTransferClient implements BlockchainTransferClient {
    /**
     * Polygon 메인넷용 실체인 클라이언트.
     * 생성된 Web3j wrapper를 사용하고, receipt에서 이벤트를 파싱한다.
     */

    private final Web3j web3j;

    @Value("${blockchain.chain-id:137}")
    private long chainId;

    @Value("${blockchain.contract.address}")
    private String contractAddress;

    @Value("${blockchain.tx.gas-limit:300000}")
    private BigInteger gasLimit;

    @Value("${blockchain.tx.receipt-interval-ms:1500}")
    private long receiptIntervalMs;

    @Value("${blockchain.tx.receipt-attempts:50}")
    private int receiptAttempts;

    @Override
    public TransferResult allocateToUser(String ownerPrivateKey, String userWalletAddress, BigInteger amount, BigInteger donationId) {
        try {
            Credentials credentials = Credentials.create(sanitizeHexKey(ownerPrivateKey));
            BigInteger gasPrice = fetchGasPrice();
            TransactionManager transactionManager = new RawTransactionManager(web3j, credentials, chainId);
            GiveNToken contract = GiveNToken.load(
                    contractAddress,
                    web3j,
                    transactionManager,
                    new StaticGasProvider(gasPrice, gasLimit)
            );
            TransactionReceipt receipt = contract.allocateToUser(userWalletAddress, amount, donationId).send();
            var events = GiveNToken.getTokenAllocatedEvents(receipt);
            if (!events.isEmpty()) {
                var event = events.get(0);
                return new TransferResult(
                        receipt.getTransactionHash(),
                        receipt.getBlockNumber() == null ? null : receipt.getBlockNumber().longValue(),
                        receipt.isStatusOK() ? "SUCCESS" : "FAIL",
                        "allocateToUser",
                        "TokenAllocated",
                        null,
                        event.userWallet,
                        event.donationId,
                        null,
                        event.amount
                );
            }
            return toResult(receipt, "allocateToUser");
        } catch (Exception e) {
            return new TransferResult(null, null, "FAIL", e.getMessage(), null, null, null, null, null, null);
        }
    }

    @Override
    public TransferResult donateToCampaign(String userPrivateKey,
                                           String campaignWalletAddress,
                                           BigInteger amount,
                                           BigInteger campaignId,
                                           BigInteger donationId) {
        try {
            Credentials credentials = Credentials.create(sanitizeHexKey(userPrivateKey));
            BigInteger gasPrice = fetchGasPrice();
            TransactionManager transactionManager = new RawTransactionManager(web3j, credentials, chainId);
            GiveNToken contract = GiveNToken.load(
                    contractAddress,
                    web3j,
                    transactionManager,
                    new StaticGasProvider(gasPrice, gasLimit)
            );
            TransactionReceipt receipt = contract.donateToCampaign(campaignWalletAddress, amount, campaignId, donationId).send();
            var events = GiveNToken.getDonationSentEvents(receipt);
            if (!events.isEmpty()) {
                var event = events.get(0);
                return new TransferResult(
                        receipt.getTransactionHash(),
                        receipt.getBlockNumber() == null ? null : receipt.getBlockNumber().longValue(),
                        receipt.isStatusOK() ? "SUCCESS" : "FAIL",
                        "donateToCampaign",
                        "DonationSent",
                        event.donorWallet,
                        event.campaignWallet,
                        event.donationId,
                        event.campaignId,
                        event.amount
                );
            }
            return toResult(receipt, "donateToCampaign");
        } catch (Exception e) {
            return new TransferResult(null, null, "FAIL", e.getMessage(), null, null, null, null, null, null);
        }
    }

    @Override
    public TransferResult transferNative(String fromAddress, String decryptedPrivateKey, String toAddress, BigInteger amountWei) {
        Credentials credentials = Credentials.create(sanitizeHexKey(decryptedPrivateKey));
        TransactionManager transactionManager = new RawTransactionManager(web3j, credentials, chainId);
        BigInteger gasPrice = fetchGasPrice();

        try {
            EthSendTransaction send = transactionManager.sendTransaction(gasPrice, BigInteger.valueOf(21_000), toAddress, "", amountWei);
            if (send.hasError()) {
                return new TransferResult(null, null, "FAIL", send.getError().getMessage(), null, null, null, null, null, null);
            }
            TransactionReceipt receipt = waitReceipt(send.getTransactionHash());
            return new TransferResult(
                    receipt.getTransactionHash(),
                    receipt.getBlockNumber() == null ? null : receipt.getBlockNumber().longValue(),
                    receipt.isStatusOK() ? "SUCCESS" : "FAIL",
                    "native transfer",
                    "NativeTransfer",
                    fromAddress,
                    toAddress,
                    null,
                    null,
                    amountWei
            );
        } catch (Exception e) {
            return new TransferResult(null, null, "FAIL", e.getMessage(), null, null, null, null, null, null);
        }
    }

    /**
     * 폴링으로 receipt를 기다리고, 실패 시 1회 직접 조회로 재확인한다.
     */
    private TransactionReceipt waitReceipt(String txHash) throws Exception {
        TransactionReceiptProcessor processor = new PollingTransactionReceiptProcessor(web3j, receiptIntervalMs, receiptAttempts);
        TransactionReceipt receipt = processor.waitForTransactionReceipt(txHash);
        if (receipt == null) {
            EthGetTransactionReceipt queried = web3j.ethGetTransactionReceipt(txHash).send();
            Optional<TransactionReceipt> optional = queried.getTransactionReceipt();
            if (optional.isPresent()) {
                return optional.get();
            }
            throw new IllegalStateException("transaction receipt not found: " + txHash);
        }
        return receipt;
    }

    /**
     * 예상 이벤트가 receipt에 없을 때 사용하는 기본 결과.
     */
    private TransferResult toResult(TransactionReceipt receipt, String opName) {
        boolean success = receipt.isStatusOK();
        return new TransferResult(
                receipt.getTransactionHash(),
                receipt.getBlockNumber() == null ? null : receipt.getBlockNumber().longValue(),
                success ? "SUCCESS" : "FAIL",
                opName,
                null,
                null,
                null,
                null,
                null,
                null
        );
    }

    /**
     * 네트워크의 현재 gasPrice를 사용한다.
     */
    private BigInteger fetchGasPrice() {
        try {
            return web3j.ethGasPrice().send().getGasPrice();
        } catch (Exception e) {
            throw new IllegalStateException("failed to fetch gas price", e);
        }
    }

    /**
     * 개인키 문자열이 0x 포함/미포함 모두 들어와도 처리한다.
     */
    private String sanitizeHexKey(String privateKeyHex) {
        if (privateKeyHex == null || privateKeyHex.isBlank()) {
            throw new IllegalArgumentException("private key is empty");
        }
        return privateKeyHex.startsWith("0x") ? privateKeyHex.substring(2) : privateKeyHex;
    }
}

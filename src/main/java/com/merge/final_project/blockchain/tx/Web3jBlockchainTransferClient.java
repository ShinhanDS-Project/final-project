package com.merge.final_project.blockchain.tx;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.EthGetTransactionReceipt;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tx.RawTransactionManager;
import org.web3j.tx.TransactionManager;
import org.web3j.tx.response.PollingTransactionReceiptProcessor;
import org.web3j.tx.response.TransactionReceiptProcessor;

import java.math.BigInteger;
import java.util.Collections;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "blockchain.stub.enabled", havingValue = "false", matchIfMissing = true)
public class Web3jBlockchainTransferClient implements BlockchainTransferClient {

    private final Web3j web3j;

    @Value("${blockchain.chain-id:137}")
    private long chainId;

    @Value("${blockchain.contract.address:${blockchain.contract.donation-token-address}}")
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
            TransactionManager txManager = new RawTransactionManager(web3j, credentials, chainId);

            Function function = new Function(
                    "allocateToUser",
                    java.util.List.of(new Address(userWalletAddress), new Uint256(amount), new Uint256(donationId)),
                    Collections.emptyList()
            );
            EthSendTransaction send = txManager.sendTransaction(
                    fetchGasPrice(),
                    gasLimit,
                    contractAddress,
                    FunctionEncoder.encode(function),
                    BigInteger.ZERO
            );
            if (send.hasError()) {
                return new TransferResult(null, null, "FAIL", send.getError().getMessage(), "ALLOCATION", null, userWalletAddress, donationId, null, amount);
            }
            TransactionReceipt receipt = waitReceipt(send.getTransactionHash());
            return new TransferResult(
                    receipt.getTransactionHash(),
                    toLong(receipt.getBlockNumber()),
                    receipt.isStatusOK() ? "SUCCESS" : "FAIL",
                    "allocateToUser",
                    "ALLOCATION",
                    null,
                    userWalletAddress,
                    donationId,
                    null,
                    amount
            );
        } catch (Exception e) {
            return new TransferResult(null, null, "FAIL", e.getMessage(), "ALLOCATION", null, userWalletAddress, donationId, null, amount);
        }
    }

    @Override
    public TransferResult donateToCampaign(
            String userPrivateKey,
            String campaignWalletAddress,
            BigInteger amount,
            BigInteger campaignId,
            BigInteger donationId
    ) {
        try {
            Credentials credentials = Credentials.create(sanitizeHexKey(userPrivateKey));
            TransactionManager txManager = new RawTransactionManager(web3j, credentials, chainId);

            Function function = new Function(
                    "donateToCampaign",
                    java.util.List.of(
                            new Address(campaignWalletAddress),
                            new Uint256(amount),
                            new Uint256(campaignId),
                            new Uint256(donationId)
                    ),
                    Collections.emptyList()
            );
            EthSendTransaction send = txManager.sendTransaction(
                    fetchGasPrice(),
                    gasLimit,
                    contractAddress,
                    FunctionEncoder.encode(function),
                    BigInteger.ZERO
            );
            if (send.hasError()) {
                return new TransferResult(null, null, "FAIL", send.getError().getMessage(), "DONATION", null, campaignWalletAddress, donationId, campaignId, amount);
            }
            TransactionReceipt receipt = waitReceipt(send.getTransactionHash());
            return new TransferResult(
                    receipt.getTransactionHash(),
                    toLong(receipt.getBlockNumber()),
                    receipt.isStatusOK() ? "SUCCESS" : "FAIL",
                    "donateToCampaign",
                    "DONATION",
                    credentials.getAddress(),
                    campaignWalletAddress,
                    donationId,
                    campaignId,
                    amount
            );
        } catch (Exception e) {
            return new TransferResult(null, null, "FAIL", e.getMessage(), "DONATION", null, campaignWalletAddress, donationId, campaignId, amount);
        }
    }

    @Override
    public TransferResult transferNative(String fromAddress, String decryptedPrivateKey, String toAddress, BigInteger amountWei) {
        Credentials credentials = Credentials.create(sanitizeHexKey(decryptedPrivateKey));
        TransactionManager txManager = new RawTransactionManager(web3j, credentials, chainId);
        BigInteger gasPrice = fetchGasPrice();

        try {
            EthSendTransaction send = txManager.sendTransaction(gasPrice, BigInteger.valueOf(21_000), toAddress, "", amountWei);
            if (send.hasError()) {
                return new TransferResult(null, null, "FAIL", send.getError().getMessage(), "ALLOCATION", fromAddress, toAddress, null, null, amountWei);
            }
            TransactionReceipt receipt = waitReceipt(send.getTransactionHash());
            return new TransferResult(
                    receipt.getTransactionHash(),
                    toLong(receipt.getBlockNumber()),
                    receipt.isStatusOK() ? "SUCCESS" : "FAIL",
                    "native transfer",
                    "ALLOCATION",
                    fromAddress,
                    toAddress,
                    null,
                    null,
                    amountWei
            );
        } catch (Exception e) {
            return new TransferResult(null, null, "FAIL", e.getMessage(), "ALLOCATION", fromAddress, toAddress, null, null, amountWei);
        }
    }

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

    private BigInteger fetchGasPrice() {
        try {
            return web3j.ethGasPrice().send().getGasPrice();
        } catch (Exception e) {
            throw new IllegalStateException("failed to fetch gas price", e);
        }
    }

    private String sanitizeHexKey(String privateKeyHex) {
        if (privateKeyHex == null || privateKeyHex.isBlank()) {
            throw new IllegalArgumentException("private key is empty");
        }
        return privateKeyHex.startsWith("0x") ? privateKeyHex.substring(2) : privateKeyHex;
    }

    private Long toLong(BigInteger value) {
        return value == null ? null : value.longValue();
    }
}

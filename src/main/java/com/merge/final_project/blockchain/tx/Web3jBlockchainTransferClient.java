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
@ConditionalOnProperty(name = "blockchain.stub.enabled", havingValue = "false")
public class Web3jBlockchainTransferClient implements BlockchainTransferClient {

    private final Web3j web3j;

    @Value("${blockchain.chain-id:0}")
    private long chainId;

    @Value("${blockchain.contract.address:${blockchain.contract.donation-token-address}}")
    private String contractAddress;

    @Value("${blockchain.tx.gas-limit:120000}")
    private BigInteger gasLimit;

    @Value("${blockchain.tx.receipt-interval-ms:1500}")
    private long receiptIntervalMs;

    @Value("${blockchain.tx.receipt-attempts:50}")
    private int receiptAttempts;

    /**
     * DonationToken 컨트랙트의 allocateToUser 함수를 호출한다.
     * 실패 시 예외를 외부로 던지지 않고 TransferResult(status=FAIL)로 반환해
     * 상위 서비스가 이력 저장을 계속 진행할 수 있게 한다.
     */
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

    /**
     * DonationToken 컨트랙트의 donateToCampaign 함수를 호출한다.
     * 기부자 지갑이 직접 서명하며, 캠페인/기부 식별자를 함께 넘긴다.
     */
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

    /**
     * 네이티브 코인 전송(가스 충전 용도).
     * value 전송만 수행하므로 data 필드는 빈 문자열을 사용한다.
     */
    @Override
    public TransferResult transferNative(String fromAddress, String decryptedPrivateKey, String toAddress, BigInteger amountWei) {
        try {
            Credentials credentials = Credentials.create(sanitizeHexKey(decryptedPrivateKey));
            TransactionManager txManager = new RawTransactionManager(web3j, credentials, chainId);
            BigInteger gasPrice = fetchGasPrice();
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

    /**
     * txHash의 영수증을 폴링 방식으로 대기 조회한다.
     * 폴링 결과가 null이면 즉시 조회를 한 번 더 시도한 뒤 없으면 예외 처리한다.
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
     * 현재 네트워크의 가스 가격을 조회한다.
     */
    private BigInteger fetchGasPrice() {
        try {
            return web3j.ethGasPrice().send().getGasPrice();
        } catch (Exception e) {
            throw new IllegalStateException("failed to fetch gas price", e);
        }
    }

    /**
     * private key 문자열의 0x prefix를 제거해 web3j 입력 형식으로 맞춘다.
     */
    private String sanitizeHexKey(String privateKeyHex) {
        if (privateKeyHex == null || privateKeyHex.isBlank()) {
            throw new IllegalArgumentException("private key is empty");
        }
        return privateKeyHex.startsWith("0x") ? privateKeyHex.substring(2) : privateKeyHex;
    }

    /**
     * BigInteger 블록 번호를 Long으로 변환한다.
     */
    private Long toLong(BigInteger value) {
        return value == null ? null : value.longValue();
    }
}

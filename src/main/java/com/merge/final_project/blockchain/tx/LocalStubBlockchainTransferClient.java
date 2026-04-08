package com.merge.final_project.blockchain.tx;

import org.springframework.context.annotation.Primary;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.math.BigInteger;
import java.util.concurrent.ThreadLocalRandom;

@Primary
@Component
@ConditionalOnProperty(name = "blockchain.stub.enabled", havingValue = "true")
public class LocalStubBlockchainTransferClient implements BlockchainTransferClient {
    /**
     * 체인 호출 없이도 통합 테스트/로컬 검증을 할 수 있도록
     * 예측 가능한 결과를 반환하는 스텁 구현.
     */

    @Override
    public TransferResult allocateToUser(String ownerPrivateKey,
                                         String userWalletAddress,
                                         BigInteger amount,
                                         BigInteger donationId) {
        String txHash = "0x" + Long.toHexString(ThreadLocalRandom.current().nextLong()) + Long.toHexString(System.nanoTime());
        long blockNumber = ThreadLocalRandom.current().nextLong(1_000_000L, 2_000_000L);
        return new TransferResult(
                txHash,
                blockNumber,
                "SUCCESS",
                "stub allocateToUser",
                "TokenAllocated",
                null,
                userWalletAddress,
                donationId,
                null,
                amount
        );
    }

    @Override
    public TransferResult donateToCampaign(String userPrivateKey,
                                           String campaignWalletAddress,
                                           BigInteger amount,
                                           BigInteger campaignId,
                                           BigInteger donationId) {
        String txHash = "0x" + Long.toHexString(ThreadLocalRandom.current().nextLong()) + Long.toHexString(System.nanoTime());
        long blockNumber = ThreadLocalRandom.current().nextLong(1_000_000L, 2_000_000L);
        return new TransferResult(
                txHash,
                blockNumber,
                "SUCCESS",
                "stub donateToCampaign",
                "DonationSent",
                null,
                campaignWalletAddress,
                donationId,
                campaignId,
                amount
        );
    }

    @Override
    public TransferResult transferNative(String fromAddress, String decryptedPrivateKey, String toAddress, BigInteger amountWei) {
        String txHash = "0x" + Long.toHexString(ThreadLocalRandom.current().nextLong()) + Long.toHexString(System.nanoTime());
        long blockNumber = ThreadLocalRandom.current().nextLong(1_000_000L, 2_000_000L);
        return new TransferResult(
                txHash,
                blockNumber,
                "SUCCESS",
                "stub native transfer",
                "NativeTransfer",
                fromAddress,
                toAddress,
                null,
                null,
                amountWei
        );
    }
}

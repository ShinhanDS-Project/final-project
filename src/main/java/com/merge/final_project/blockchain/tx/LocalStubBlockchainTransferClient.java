package com.merge.final_project.blockchain.tx;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.math.BigInteger;
import java.util.UUID;

@Component
@ConditionalOnProperty(name = "blockchain.stub.enabled", havingValue = "true")
public class LocalStubBlockchainTransferClient implements BlockchainTransferClient {

    @Override
    public TransferResult allocateToUser(String ownerPrivateKey, String userWalletAddress, BigInteger amount, BigInteger donationId) {
        return new TransferResult(
                "0xstub-" + UUID.randomUUID(),
                0L,
                "SUCCESS",
                "stub transfer",
                "ALLOCATION",
                null,
                userWalletAddress,
                donationId,
                null,
                amount
        );
    }

    @Override
    public TransferResult donateToCampaign(String userPrivateKey, String campaignWalletAddress, BigInteger amount, BigInteger campaignId, BigInteger donationId) {
        return new TransferResult(
                "0xstub-" + UUID.randomUUID(),
                0L,
                "SUCCESS",
                "stub transfer",
                "DONATION",
                null,
                campaignWalletAddress,
                donationId,
                campaignId,
                amount
        );
    }

    @Override
    public TransferResult transferNative(String fromAddress, String decryptedPrivateKey, String toAddress, BigInteger amountWei) {
        return new TransferResult(
                "0xstub-" + UUID.randomUUID(),
                0L,
                "SUCCESS",
                "stub native transfer",
                "ALLOCATION",
                fromAddress,
                toAddress,
                null,
                null,
                amountWei
        );
    }
}

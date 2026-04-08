package com.merge.final_project.blockchain.tx;

import java.math.BigInteger;

public interface BlockchainTransferClient {

    TransferResult allocateToUser(String ownerPrivateKey, String userWalletAddress, BigInteger amount, BigInteger donationId);

    TransferResult donateToCampaign(
            String userPrivateKey,
            String campaignWalletAddress,
            BigInteger amount,
            BigInteger campaignId,
            BigInteger donationId
    );

    TransferResult transferNative(String fromAddress, String decryptedPrivateKey, String toAddress, BigInteger amountWei);
}

package com.merge.final_project.blockchain.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tx.gas.DefaultGasProvider;

import java.math.BigInteger;

@Service
@RequiredArgsConstructor
public class BlockchainService {

    private final Web3j web3j;

    @Value("${blockchain.contract.donation-token-address}")
    private String contractAddress;

    public TransactionReceipt settleCampaignOnChain(
            String campaignPrivateKey,
            String charityWalletAddress,
            String beneficiaryWalletAddress,
            BigInteger totalAmount,
            BigInteger feeBps,
            BigInteger campaignId,
            BigInteger settlementId
    ) throws Exception {
        Credentials credentials = Credentials.create(campaignPrivateKey);

        DonationToken contract = DonationToken.load(
                contractAddress,
                web3j,
                credentials,
                new DefaultGasProvider()
        );
        return contract.settleCampaign(
                charityWalletAddress,
                beneficiaryWalletAddress,
                totalAmount,
                feeBps,
                campaignId,
                settlementId
        ).send();
    }

}

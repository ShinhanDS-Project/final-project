package com.merge.final_project.blockchain.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tx.RawTransactionManager;
import org.web3j.tx.TransactionManager;
import org.web3j.tx.gas.ContractGasProvider;

import java.math.BigInteger;

@Service
@RequiredArgsConstructor
public class BlockchainService {

    private final Web3j web3j;
    private final ContractGasProvider contractGasProvider;

    @Value("${blockchain.contract.donation-token-address}")
    private String contractAddress;

    @Value("${blockchain.chain-id:0}")
    private long chainId;

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
        TransactionManager txManager = new RawTransactionManager(web3j, credentials, chainId);

        DonationToken contract = DonationToken.load(
                contractAddress,
                web3j,
                txManager,
                contractGasProvider
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

    public TransactionReceipt redeemOnChain(
            String requesterPrivateKey,
            BigInteger amount,
            BigInteger redemptionId
    ) throws Exception {
        Credentials credentials = Credentials.create(requesterPrivateKey);
        TransactionManager txManager = new RawTransactionManager(web3j, credentials, chainId);

        DonationToken contract = DonationToken.load(
                contractAddress,
                web3j,
                txManager,
                contractGasProvider
        );

        return contract.returnForRedemption(amount, redemptionId).send();
    }

    public BigInteger getTokenBalance(String walletAddress) throws Exception {
        DonationToken contract = DonationToken.load(
                contractAddress,
                web3j,
                Credentials.create(String.format("%064x", BigInteger.ONE)),
                contractGasProvider
        );
        return contract.balanceOf(walletAddress).send();
    }
}

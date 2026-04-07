package com.merge.final_project.blockchain.service;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.RemoteCall;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tx.Contract;
import org.web3j.tx.TransactionManager;
import org.web3j.tx.gas.ContractGasProvider;

public class DonationToken extends Contract {

    public static final String BINARY = "";
    private static final String FUNC_SETTLE_CAMPAIGN = "settleCampaign";

    protected DonationToken(
            String contractAddress,
            Web3j web3j,
            Credentials credentials,
            ContractGasProvider contractGasProvider
    ) {
        super(BINARY, contractAddress, web3j, credentials, contractGasProvider);
    }

    protected DonationToken(
            String contractAddress,
            Web3j web3j,
            TransactionManager transactionManager,
            ContractGasProvider contractGasProvider
    ) {
        super(BINARY, contractAddress, web3j, transactionManager, contractGasProvider);
    }

    public static DonationToken load(
            String contractAddress,
            Web3j web3j,
            Credentials credentials,
            ContractGasProvider contractGasProvider
    ) {
        return new DonationToken(contractAddress, web3j, credentials, contractGasProvider);
    }

    public static DonationToken load(
            String contractAddress,
            Web3j web3j,
            TransactionManager transactionManager,
            ContractGasProvider contractGasProvider
    ) {
        return new DonationToken(contractAddress, web3j, transactionManager, contractGasProvider);
    }

    public RemoteCall<TransactionReceipt> settleCampaign(
            String charityWallet,
            String beneficiaryWallet,
            BigInteger totalAmount,
            BigInteger feeBps,
            BigInteger campaignId,
            BigInteger settlementId
    ) {
        Function function = new Function(
                FUNC_SETTLE_CAMPAIGN,
                Arrays.asList(
                        new Address(charityWallet),
                        new Address(beneficiaryWallet),
                        new Uint256(totalAmount),
                        new Uint256(feeBps),
                        new Uint256(campaignId),
                        new Uint256(settlementId)
                ),
                Collections.<TypeReference<?>>emptyList()
        );

        return executeRemoteCallTransaction(function);
    }
}

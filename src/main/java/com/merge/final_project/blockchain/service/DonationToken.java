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
    // Solidity 컨트랙트의 settleCampaign 함수명과 맞춰 호출한다.
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
        // 배포된 컨트랙트 주소에 바인딩해서 이후 함수 호출에 사용한다.
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
        // 컨트랙트 함수 인자 순서를 Solidity 시그니처와 동일하게 맞춘다.
        // feeBps 는 퍼센트가 아니라 basis points 단위 값이다.
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

        // send() 시점에 실제 트랜잭션이 전송되고, 여기서는 호출 객체만 생성한다.
        return executeRemoteCallTransaction(function);
    }
}

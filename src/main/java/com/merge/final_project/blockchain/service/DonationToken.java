package com.merge.final_project.blockchain.service;

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

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;

public class DonationToken extends Contract {

    public static final String BINARY = "";

    // Solidity 컨트랙트의 함수명과 동일해야 web3j Function 호출이 정상 동작한다.
    private static final String FUNC_SETTLE_CAMPAIGN = "settleCampaign";
    private static final String FUNC_RETURN_FOR_REDEMPTION = "returnForRedemption";
    private static final String FUNC_BALANCE_OF = "balanceOf";

    // Credentials 기반 생성자 (개인키 서명 방식)
    protected DonationToken(
            String contractAddress,
            Web3j web3j,
            Credentials credentials,
            ContractGasProvider contractGasProvider
    ) {
        super(BINARY, contractAddress, web3j, credentials, contractGasProvider);
    }

    // TransactionManager 기반 생성자 (커스텀 트랜잭션 처리)
    protected DonationToken(
            String contractAddress,
            Web3j web3j,
            TransactionManager transactionManager,
            ContractGasProvider contractGasProvider
    ) {
        super(BINARY, contractAddress, web3j, transactionManager, contractGasProvider);
    }

    // 컨트랙트 주소에 바인딩 (개인키 기반 호출)
    public static DonationToken load(
            String contractAddress,
            Web3j web3j,
            Credentials credentials,
            ContractGasProvider contractGasProvider
    ) {
        // 배포된 컨트랙트 주소에 바인딩해서 이후 함수 호출에 사용한다.
        return new DonationToken(contractAddress, web3j, credentials, contractGasProvider);
    }

    // 컨트랙트 주소에 바인딩 (TransactionManager 기반 호출)
    public static DonationToken load(
            String contractAddress,
            Web3j web3j,
            TransactionManager transactionManager,
            ContractGasProvider contractGasProvider
    ) {
        return new DonationToken(contractAddress, web3j, transactionManager, contractGasProvider);
    }

    // 캠페인 정산 트랜잭션 생성 (기부단체 → 수혜자 분배)
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

    // 현금화 트랜잭션 생성 (요청자 지갑 → 핫월렛)
    public RemoteCall<TransactionReceipt> returnForRedemption(
            BigInteger amount,
            BigInteger redemptionId
    ) {
        // 현금화는 요청자 지갑에서 hot wallet 으로 토큰을 반환하는 트랜잭션이다.
        // 인자 순서는 Solidity 함수 정의와 동일하게 amount, redemptionId 순서를 유지한다.
        Function function = new Function(
                FUNC_RETURN_FOR_REDEMPTION,
                Arrays.asList(
                        new Uint256(amount),
                        new Uint256(redemptionId)
                ),
                Collections.<TypeReference<?>>emptyList()
        );
        // 트랜잭션 호출 객체 반환 (send() 시 실제 실행)
        return executeRemoteCallTransaction(function);
    }

    public RemoteCall<BigInteger> balanceOf(String walletAddress) {
        // Solidity balanceOf(address) 함수 호출 정의
        Function function = new Function(
                FUNC_BALANCE_OF,
                Arrays.asList(new Address(walletAddress)),
                Arrays.asList(new TypeReference<Uint256>() {})
        );
        // 단일 값(Uint256 → BigInteger) 반환하는 조회(call) 실행 객체 생성
        return executeRemoteCallSingleValueReturn(function, BigInteger.class);
    }
}

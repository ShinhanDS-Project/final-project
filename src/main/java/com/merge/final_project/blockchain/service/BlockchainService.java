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

    // 배포된 DonationToken 컨트랙트 주소
    @Value("${blockchain.contract.donation-token-address}")
    private String contractAddress;

    // 캠페인 정산 온체인 호출 (기부단체 → 수혜자 토큰 분배)
    public TransactionReceipt settleCampaignOnChain(
            String campaignPrivateKey,
            String charityWalletAddress,
            String beneficiaryWalletAddress,
            BigInteger totalAmount,
            BigInteger feeBps,
            BigInteger campaignId,
            BigInteger settlementId
    ) throws Exception {
        // 개인키로 서명 객체 생성
        Credentials credentials = Credentials.create(campaignPrivateKey);

        // 컨트랙트 바인딩
        DonationToken contract = DonationToken.load(
                contractAddress,
                web3j,
                credentials,
                new DefaultGasProvider()
        );
        // 정산 트랜잭션 실행 (send() 시 실제 체인에 전송)
        return contract.settleCampaign(
                charityWalletAddress,
                beneficiaryWalletAddress,
                totalAmount,
                feeBps,
                campaignId,
                settlementId
        ).send();
    }

    // 현금화 온체인 호출 (요청자 지갑 → 핫월렛 토큰 반환)
    public TransactionReceipt redeemOnChain(
            String requesterPrivateKey,
            BigInteger amount,
            BigInteger redemptionId
    ) throws Exception {
        // 요청자 개인키로 서명 객체 생성
        Credentials credentials = Credentials.create(requesterPrivateKey);

        // 컨트랙트 바인딩
        DonationToken contract = DonationToken.load(
                contractAddress,
                web3j,
                credentials,
                new DefaultGasProvider()
        );

        // 현금화 트랜잭션 실행
        return contract.returnForRedemption(amount, redemptionId).send();
    }

    public BigInteger getTokenBalance(String walletAddress) throws Exception {
        // 컨트랙트 로드 (읽기 전용 호출이라 더미 privateKey 사용)
        DonationToken contract = DonationToken.load(
                contractAddress,
                web3j,
                Credentials.create(String.format("%064x", BigInteger.ONE)),
                new DefaultGasProvider()
        );
        // 해당 지갑의 토큰 잔액 조회 (온체인 call, 실제 트랜잭션 발생 안 함)
        return contract.balanceOf(walletAddress).send();
    }

}

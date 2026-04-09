package com.merge.final_project.blockchain.tx;

import java.math.BigInteger;

public interface BlockchainTransferClient {

    /**
     * 서버(소유자) 지갑에서 기부자 지갑으로 토큰을 배정한다.
     *
     * @param ownerPrivateKey 소유자 지갑 복호화 private key
     * @param userWalletAddress 수신자(기부자) 지갑 주소
     * @param amount 토큰 최소 단위 값(예: 18 decimals 스케일 반영)
     * @param donationId 도메인 기부 식별자
     */
    TransferResult allocateToUser(String ownerPrivateKey, String userWalletAddress, BigInteger amount, BigInteger donationId);

    /**
     * 기부자 지갑에서 캠페인 지갑으로 토큰을 이체한다.
     *
     * @param userPrivateKey 기부자 지갑 복호화 private key
     * @param campaignWalletAddress 캠페인 지갑 주소
     * @param amount 토큰 최소 단위 값
     * @param campaignId 도메인 캠페인 식별자
     * @param donationId 도메인 기부 식별자
     */
    TransferResult donateToCampaign(
            String userPrivateKey,
            String campaignWalletAddress,
            BigInteger amount,
            BigInteger campaignId,
            BigInteger donationId
    );

    /**
     * 가스 충전을 위한 네이티브 코인 전송.
     *
     * @param fromAddress 송신 지갑 주소(로그/트레이싱용)
     * @param decryptedPrivateKey 송신 지갑 복호화 private key
     * @param toAddress 수신 지갑 주소
     * @param amountWei 전송할 네이티브 코인 wei 단위 금액
     */
    TransferResult transferNative(String fromAddress, String decryptedPrivateKey, String toAddress, BigInteger amountWei);
}

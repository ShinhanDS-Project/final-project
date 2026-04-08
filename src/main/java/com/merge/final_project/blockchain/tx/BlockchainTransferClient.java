package com.merge.final_project.blockchain.tx;

import java.math.BigInteger;

/**
 * 블록체인 전송 인터페이스.
 * 구현체는 실체인(Web3j) 또는 로컬 스텁으로 교체 가능하다.
 */
public interface BlockchainTransferClient {
    /**
     * 컨트랙트 함수 allocateToUser(userWallet, amount, donationId) 호출.
     */
    TransferResult allocateToUser(String ownerPrivateKey, String userWalletAddress, BigInteger amount, BigInteger donationId);

    /**
     * 컨트랙트 함수 donateToCampaign(campaignWallet, amount, campaignId, donationId) 호출.
     */
    TransferResult donateToCampaign(String userPrivateKey,
                                    String campaignWalletAddress,
                                    BigInteger amount,
                                    BigInteger campaignId,
                                    BigInteger donationId);

    /**
     * 가스 충전을 위한 네이티브 코인(POL) 전송.
     */
    TransferResult transferNative(String fromAddress, String decryptedPrivateKey, String toAddress, BigInteger amountWei);
}

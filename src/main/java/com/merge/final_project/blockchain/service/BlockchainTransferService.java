package com.merge.final_project.blockchain.service;

import com.merge.final_project.blockchain.dto.BlockchainTransferResponse;
import com.merge.final_project.blockchain.entity.Key;
import com.merge.final_project.blockchain.entity.Transaction;
import com.merge.final_project.blockchain.gas.GasStationService;
import com.merge.final_project.blockchain.repository.KeyRepository;
import com.merge.final_project.blockchain.security.WalletCryptoService;
import com.merge.final_project.blockchain.tx.BlockchainTransferClient;
import com.merge.final_project.blockchain.tx.TransferResult;
import com.merge.final_project.campaign.campaigns.entity.Campaign;
import com.merge.final_project.campaign.campaigns.repository.CampaignRepository;
import com.merge.final_project.wallet.entity.Wallet;
import com.merge.final_project.wallet.entity.WalletType;
import com.merge.final_project.wallet.repository.WalletLookupRepository;
import com.merge.final_project.wallet.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;
import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class BlockchainTransferService {

    private final WalletRepository walletRepository;
    private final WalletLookupRepository walletLookupRepository;
    private final CampaignRepository campaignRepository;
    private final KeyRepository keyRepository;
    private final TransferTransactionService transferTransactionService;
    private final GasStationService gasStationService;
    private final BlockchainTransferClient blockchainTransferClient;
    private final WalletCryptoService walletCryptoService;

    @Value("${blockchain.token.decimals:18}")
    private int tokenDecimals;

    @Value("${blockchain.contract.owner-address:}")
    private String contractOwnerAddress;

    /**
     * 결제 승인 직후 서버(소유자) 지갑에서 기부자 지갑으로 토큰을 충전한다.
     * 처리 순서:
     * 1) 요청값 유효성 검증
     * 2) HOT/소유자/기부자 지갑 조회
     * 3) 서명 지갑 가스(POL) 사전 점검 및 필요 시 자동 충전
     * 4) 온체인 allocateToUser 호출
     * 5) 결과를 거래내역(Transaction) 테이블에 저장
     */
    @Transactional
    public BlockchainTransferResponse chargeUserToken(Long userNo, Long amount, Long donationId) {
        validatePositive(amount, "amount");
        validatePositive(userNo, "userNo");

        Wallet hotWallet = findHotWallet();
        Wallet userWallet = walletLookupRepository.findByWalletTypeAndOwnerNo(WalletType.USER, userNo)
                .orElseThrow(() -> new IllegalArgumentException("user wallet not found: " + userNo));
        Wallet ownerWallet = findContractOwnerWallet(hotWallet);
        gasStationService.ensureSufficientPol(ownerWallet);

        BigInteger onChainAmount = toOnChainTokenAmount(amount);
        BigInteger resolvedDonationId = resolveDonationId(donationId);

        TransferResult transferResult = blockchainTransferClient.allocateToUser(
                resolveWalletPrivateKey(ownerWallet),
                userWallet.getWalletAddress(),
                onChainAmount,
                resolvedDonationId
        );
        if (!"SUCCESS".equalsIgnoreCase(transferResult.status())) {
            log.warn("allocateToUser failed. userNo={}, donationId={}, message={}",
                    userNo, resolvedDonationId, transferResult.message());
        }

        Transaction saved = transferTransactionService.saveTransfer(
                hotWallet,
                userWallet,
                amount,
                transferResult.txHash(),
                transferResult.blockNumber(),
                transferResult.status(),
                "ALLOCATION"
        );

        return new BlockchainTransferResponse(
                String.valueOf(saved.getTransactionNo()),
                saved.getTxHash(),
                saved.getStatus().name(),
                saved.getEventType().name()
        );
    }

    /**
     * 기부자 지갑에서 캠페인 지갑으로 토큰을 이체한다.
     * 온체인 전송 성공/실패와 무관하게 거래내역을 저장해 추적 가능성을 보장한다.
     */
    @Transactional
    public BlockchainTransferResponse transferDonationToCampaign(Long userNo, Long campaignNo, Long amount, Long donationId) {
        validatePositive(amount, "amount");
        validatePositive(userNo, "userNo");
        validatePositive(campaignNo, "campaignNo");

        Wallet userWallet = walletLookupRepository.findByWalletTypeAndOwnerNo(WalletType.USER, userNo)
                .orElseThrow(() -> new IllegalArgumentException("user wallet not found: " + userNo));

        Campaign campaign = campaignRepository.findById(campaignNo)
                .orElseThrow(() -> new IllegalArgumentException("campaign not found: " + campaignNo));
        if (campaign.getWalletNo() == null) {
            throw new IllegalStateException("campaign wallet is not assigned: " + campaignNo);
        }
        Wallet campaignWallet = walletRepository.findById(campaign.getWalletNo())
                .orElseThrow(() -> new IllegalArgumentException("campaign wallet not found: " + campaign.getWalletNo()));
        gasStationService.ensureSufficientPol(userWallet);

        BigInteger onChainAmount = toOnChainTokenAmount(amount);
        BigInteger resolvedDonationId = resolveDonationId(donationId);

        TransferResult transferResult = blockchainTransferClient.donateToCampaign(
                resolveWalletPrivateKey(userWallet),
                campaignWallet.getWalletAddress(),
                onChainAmount,
                BigInteger.valueOf(campaignNo),
                resolvedDonationId
        );
        if (!"SUCCESS".equalsIgnoreCase(transferResult.status())) {
            log.warn("donateToCampaign failed. userNo={}, campaignNo={}, donationId={}, message={}",
                    userNo, campaignNo, resolvedDonationId, transferResult.message());
        }

        Transaction saved = transferTransactionService.saveTransfer(
                userWallet,
                campaignWallet,
                amount,
                transferResult.txHash(),
                transferResult.blockNumber(),
                transferResult.status(),
                "DONATION"
        );

        return new BlockchainTransferResponse(
                String.valueOf(saved.getTransactionNo()),
                saved.getTxHash(),
                saved.getStatus().name(),
                saved.getEventType().name()
        );
    }

    /**
     * ALLOCATION 흐름의 기본 송신 지갑(HOT)을 조회한다.
     */
    private Wallet findHotWallet() {
        return walletLookupRepository.findFirstByWalletType(WalletType.HOT)
                .orElseThrow(() -> new IllegalStateException("HOT wallet not found"));
    }

    /**
     * 컨트랙트 소유자 주소가 설정된 경우 해당 지갑을 서명 지갑으로 사용한다.
     * 설정이 없으면 HOT 지갑으로 대체한다.
     */
    private Wallet findContractOwnerWallet(Wallet hotWallet) {
        if (contractOwnerAddress == null || contractOwnerAddress.isBlank()) {
            return hotWallet;
        }
        return walletLookupRepository.findByWalletAddressIgnoreCase(contractOwnerAddress)
                .orElseThrow(() -> new IllegalStateException("contract owner wallet not found in DB: " + contractOwnerAddress));
    }

    /**
     * 비즈니스 금액(Long)을 토큰 최소 단위(10^decimals)로 변환한다.
     */
    private BigInteger toOnChainTokenAmount(Long amount) {
        return BigInteger.valueOf(amount).multiply(BigInteger.TEN.pow(tokenDecimals));
    }

    /**
     * donationId가 유효하면 그대로 사용하고,
     * 없으면 epoch millis 기반 임시 ID를 생성해 컨트랙트 인자를 항상 채운다.
     */
    private BigInteger resolveDonationId(Long donationId) {
        if (donationId != null && donationId > 0) {
            return BigInteger.valueOf(donationId);
        }
        return BigInteger.valueOf(Instant.now().toEpochMilli());
    }

    /**
     * 지갑의 private key payload를 조회/복호화한다.
     * 레거시 평문 키 데이터와의 호환을 위해 복호화 실패 시 원문 문자열을 그대로 반환한다.
     */
    private String resolveWalletPrivateKey(Wallet wallet) {
        if (wallet.getKey() == null || wallet.getKey().getKeyNo() == null) {
            throw new IllegalStateException("wallet key reference is missing. walletNo=" + wallet.getWalletNo());
        }
        Key key = keyRepository.findById(wallet.getKey().getKeyNo())
                .orElseThrow(() -> new IllegalStateException("key row not found: " + wallet.getKey().getKeyNo()));

        String storedPrivateKey = key.getPrivateKey();
        if (storedPrivateKey == null || storedPrivateKey.isBlank()) {
            throw new IllegalStateException("private key is empty. keyNo=" + key.getKeyNo());
        }

        try {
            return walletCryptoService.decryptPrivateKey(storedPrivateKey);
        } catch (RuntimeException e) {
            log.warn("keyNo={} is not decryptable payload. fallback to raw key string.", key.getKeyNo());
            return storedPrivateKey;
        }
    }

    /**
     * 식별자/금액 공통 양수 검증 유틸.
     */
    private void validatePositive(Long value, String fieldName) {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException(fieldName + " must be positive");
        }
    }
}

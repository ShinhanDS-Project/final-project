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
        Wallet campaignWallet = walletRepository.findByWalletNo(campaign.getWalletNo())
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

    private Wallet findHotWallet() {
        return walletLookupRepository.findFirstByWalletType(WalletType.HOT)
                .orElseThrow(() -> new IllegalStateException("HOT wallet not found"));
    }

    private Wallet findContractOwnerWallet(Wallet hotWallet) {
        if (contractOwnerAddress == null || contractOwnerAddress.isBlank()) {
            return hotWallet;
        }
        return walletLookupRepository.findByWalletAddressIgnoreCase(contractOwnerAddress)
                .orElseThrow(() -> new IllegalStateException("contract owner wallet not found in DB: " + contractOwnerAddress));
    }

    private BigInteger toOnChainTokenAmount(Long amount) {
        return BigInteger.valueOf(amount).multiply(BigInteger.TEN.pow(tokenDecimals));
    }

    private BigInteger resolveDonationId(Long donationId) {
        if (donationId != null && donationId > 0) {
            return BigInteger.valueOf(donationId);
        }
        return BigInteger.valueOf(Instant.now().toEpochMilli());
    }

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

    private void validatePositive(Long value, String fieldName) {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException(fieldName + " must be positive");
        }
    }
}

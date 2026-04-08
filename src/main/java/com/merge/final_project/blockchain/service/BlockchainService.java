package com.merge.final_project.blockchain.service;

import com.merge.final_project.blockchain.dto.BlockchainTransferResponse;
import com.merge.final_project.blockchain.gas.GasStationService;
import com.merge.final_project.blockchain.security.WalletCryptoService;
import com.merge.final_project.blockchain.tx.BlockchainTransferClient;
import com.merge.final_project.blockchain.tx.TransferResult;
import com.merge.final_project.db.entity.Campaign;
import com.merge.final_project.db.entity.KeyEntity;
import com.merge.final_project.db.entity.Wallet;
import com.merge.final_project.db.repository.CampaignRepository;
import com.merge.final_project.db.repository.KeyEntityRepository;
import com.merge.final_project.db.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tx.gas.DefaultGasProvider;

import java.math.BigInteger;
import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class BlockchainService {

    private static final String WALLET_TYPE_SERVER = "SERVER";
    private static final String WALLET_TYPE_USER = "USER";
    private static final String SERVER_OWNER_HOT = "HOT";

    private final Web3j web3j;
    private final WalletRepository walletRepository;
    private final CampaignRepository campaignRepository;
    private final KeyEntityRepository keyEntityRepository;
    private final TransactionService transactionService;
    private final EventService eventService;
    private final GasStationService gasStationService;
    private final BlockchainTransferClient blockchainTransferClient;
    private final WalletCryptoService walletCryptoService;

    @Value("${blockchain.contract.donation-token-address}")
    private String contractAddress;

    @Value("${blockchain.token.decimals:18}")
    private int tokenDecimals;

    @Value("${blockchain.contract.owner-address:}")
    private String contractOwnerAddress;

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

    @Transactional
    public BlockchainTransferResponse chargeUserToken(Integer userNo, Long amount, Long donationId) {
        validatePositive(amount, "amount");

        Wallet hotWallet = findServerHotWallet();
        Wallet userWallet = walletRepository.findByWalletTypeAndOwnerNo(WALLET_TYPE_USER, userNo.toString())
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

        var saved = transactionService.saveTransfer(
                hotWallet,
                userWallet,
                amount,
                transferResult.txHash(),
                transferResult.blockNumber(),
                transferResult.status(),
                "PAYMENT_TOKEN_CHARGE"
        );
        saved = eventService.applyParsedEvent(saved, transferResult, tokenDecimals);
        return new BlockchainTransferResponse(
                String.valueOf(saved.getTransactionNo()),
                saved.getTxHash(),
                saved.getStatus(),
                saved.getEventType()
        );
    }

    @Transactional
    public BlockchainTransferResponse transferDonationToCampaign(Integer userNo, Long campaignNo, Long amount, Long donationId) {
        validatePositive(amount, "amount");

        Wallet userWallet = walletRepository.findByWalletTypeAndOwnerNo(WALLET_TYPE_USER, userNo.toString())
                .orElseThrow(() -> new IllegalArgumentException("user wallet not found: " + userNo));

        Integer campaignNoInt = safeLongToInt(campaignNo, "campaignNo");
        Campaign campaign = campaignRepository.findById(campaignNoInt)
                .orElseThrow(() -> new IllegalArgumentException("campaign not found: " + campaignNo));
        Long campaignWalletNo = parseWalletNo(campaign.getWalletNo(), campaignNo);
        if (campaignWalletNo == null) {
            throw new IllegalStateException("campaign wallet is not assigned: " + campaignNo);
        }
        Wallet campaignWallet = walletRepository.findByIdWalletNo(campaignWalletNo)
                .orElseThrow(() -> new IllegalArgumentException("campaign wallet not found: " + campaignWalletNo));
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

        var saved = transactionService.saveTransfer(
                userWallet,
                campaignWallet,
                amount,
                transferResult.txHash(),
                transferResult.blockNumber(),
                transferResult.status(),
                "DONATION_TRANSFER"
        );
        saved = eventService.applyParsedEvent(saved, transferResult, tokenDecimals);
        return new BlockchainTransferResponse(
                String.valueOf(saved.getTransactionNo()),
                saved.getTxHash(),
                saved.getStatus(),
                saved.getEventType()
        );
    }

    private Wallet findServerHotWallet() {
        return walletRepository.findByWalletTypeAndOwnerNo(WALLET_TYPE_SERVER, SERVER_OWNER_HOT)
                .orElseThrow(() -> new IllegalStateException("server HOT wallet not found"));
    }

    private Wallet findContractOwnerWallet(Wallet hotWallet) {
        if (contractOwnerAddress == null || contractOwnerAddress.isBlank()) {
            return hotWallet;
        }
        return walletRepository.findByWalletAddressIgnoreCase(contractOwnerAddress)
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
        if (wallet.getId() == null || wallet.getId().getKeyNo() == null) {
            throw new IllegalStateException("wallet key reference is missing. walletAddress=" + wallet.getWalletAddress());
        }

        KeyEntity keyEntity = keyEntityRepository.findById(wallet.getId().getKeyNo())
                .orElseThrow(() -> new IllegalStateException("key row not found: " + wallet.getId().getKeyNo()));

        String storedPrivateKey = keyEntity.getPrivateKey();
        if (storedPrivateKey == null || storedPrivateKey.isBlank()) {
            throw new IllegalStateException("private key is empty. keyNo=" + keyEntity.getKeyNo());
        }

        try {
            return walletCryptoService.decryptPrivateKey(storedPrivateKey);
        } catch (RuntimeException e) {
            log.warn("keyNo={} is not decryptable payload. fallback to raw key string.", keyEntity.getKeyNo());
            return storedPrivateKey;
        }
    }

    private Long parseWalletNo(String walletNo, Long campaignNo) {
        if (walletNo == null || walletNo.isBlank()) {
            return null;
        }
        try {
            return Long.parseLong(walletNo.trim());
        } catch (NumberFormatException e) {
            throw new IllegalStateException("campaign.wallet_no is not numeric. campaignNo=" + campaignNo + ", walletNo=" + walletNo);
        }
    }

    private Integer safeLongToInt(Long value, String fieldName) {
        if (value == null) {
            throw new IllegalArgumentException(fieldName + " is required");
        }
        if (value > Integer.MAX_VALUE || value < Integer.MIN_VALUE) {
            throw new IllegalArgumentException(fieldName + " out of int range: " + value);
        }
        return value.intValue();
    }

    private void validatePositive(Long value, String fieldName) {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException(fieldName + " must be positive");
        }
    }
}

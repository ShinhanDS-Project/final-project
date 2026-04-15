package com.merge.final_project.blockchain.service;

import com.merge.final_project.blockchain.dto.BlockchainTransferResponse;
import com.merge.final_project.blockchain.entity.Transaction;
import com.merge.final_project.blockchain.gas.GasStationService;
import com.merge.final_project.blockchain.security.WalletPrivateKeyResolver;
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

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class BlockchainTransferService {

    private final WalletRepository walletRepository;
    private final WalletLookupRepository walletLookupRepository;
    private final CampaignRepository campaignRepository;
    private final TransferTransactionService transferTransactionService;
    private final GasStationService gasStationService;
    private final BlockchainTransferClient blockchainTransferClient;
    private final WalletPrivateKeyResolver walletPrivateKeyResolver;
    private final TokenAmountConverter tokenAmountConverter;

    @Value("${blockchain.contract.owner-address:}")
    private String contractOwnerAddress;

    @Value("${blockchain.wallet.hot-address:}")
    private String hotWalletAddress;

    @Transactional
    public BlockchainTransferResponse chargeUserToken(Long userNo, Long amount, Long donationId) {
        // 서버(오너) -> 기부자 지갑 토큰 충전 경로
        validatePositive(amount, "amount");
        validatePositive(userNo, "userNo");

        Wallet hotWallet = findHotWallet();
        Wallet userWallet = walletLookupRepository.findByWalletTypeAndOwnerNo(WalletType.USER, userNo)
                .orElseThrow(() -> new IllegalArgumentException("user wallet not found: " + userNo));
        Wallet ownerWallet = findContractOwnerWallet(hotWallet);
        gasStationService.ensureSufficientPol(ownerWallet);

        BigInteger onChainAmount = tokenAmountConverter.toOnChainAmount(amount);
        BigInteger resolvedDonationId = resolveDonationId(donationId);

        TransferResult transferResult = blockchainTransferClient.allocateToUser(
                walletPrivateKeyResolver.resolveForWallet(ownerWallet),
                userWallet.getWalletAddress(),
                onChainAmount,
                resolvedDonationId
        );
        if ("SUCCESS".equalsIgnoreCase(transferResult.status())) {
            // 온체인 성공 시 HOT 감소 / USER 증가를 DB에 즉시 반영한다.
            applyChargeWalletBalanceUpdate(hotWallet, userWallet, amount);
        } else {
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
        // 기부자 지갑 -> 캠페인 지갑 기부 전송 경로
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

        BigInteger onChainAmount = tokenAmountConverter.toOnChainAmount(amount);
        BigInteger resolvedDonationId = resolveDonationId(donationId);

        TransferResult transferResult = blockchainTransferClient.donateToCampaign(
                walletPrivateKeyResolver.resolveForWallet(userWallet),
                campaignWallet.getWalletAddress(),
                onChainAmount,
                BigInteger.valueOf(campaignNo),
                resolvedDonationId
        );
        if ("SUCCESS".equalsIgnoreCase(transferResult.status())) {
            // 이 경로는 캠페인 지갑 잔액이 누적 증가한다.
            applyCampaignWalletBalanceIncrease(campaignWallet, amount);
        } else {
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
        if (hotWalletAddress == null || hotWalletAddress.isBlank()) {
            throw new IllegalStateException("configured hot wallet address is empty");
        }
        Wallet hotWallet = walletLookupRepository.findByWalletAddressIgnoreCase(hotWalletAddress)
                .orElseThrow(() -> new IllegalStateException("HOT wallet not found by configured address: " + hotWalletAddress));
        if (hotWallet.getWalletType() != WalletType.HOT) {
            throw new IllegalStateException("configured hot wallet address is not HOT type: " + hotWalletAddress);
        }
        return hotWallet;
    }

    private Wallet findContractOwnerWallet(Wallet hotWallet) {
        if (contractOwnerAddress == null || contractOwnerAddress.isBlank()) {
            return hotWallet;
        }
        return walletLookupRepository.findByWalletAddressIgnoreCase(contractOwnerAddress)
                .orElseThrow(() -> new IllegalStateException("contract owner wallet not found in DB: " + contractOwnerAddress));
    }

    private BigInteger resolveDonationId(Long donationId) {
        // donationId 미지정 시에도 추적 가능하도록 epoch millis를 대체 id로 사용한다.
        if (donationId != null && donationId > 0) {
            return BigInteger.valueOf(donationId);
        }
        return BigInteger.valueOf(Instant.now().toEpochMilli());
    }

    private void applyCampaignWalletBalanceIncrease(Wallet campaignWallet, Long amount) {
        BigDecimal current = campaignWallet.getBalance() == null ? BigDecimal.ZERO : campaignWallet.getBalance();
        campaignWallet.updateBalance(current.add(BigDecimal.valueOf(amount)));
        campaignWallet.updateLastUsedAt();
    }

    private void applyChargeWalletBalanceUpdate(Wallet hotWallet, Wallet userWallet, Long amount) {
        // 충전 성공 시 HOT 지갑은 차감되고 USER 지갑은 동일 금액만큼 증가한다.
        BigDecimal delta = BigDecimal.valueOf(amount);

        BigDecimal hotCurrent = hotWallet.getBalance() == null ? BigDecimal.ZERO : hotWallet.getBalance();
        hotWallet.updateBalance(hotCurrent.subtract(delta));
        hotWallet.updateLastUsedAt();

        BigDecimal userCurrent = userWallet.getBalance() == null ? BigDecimal.ZERO : userWallet.getBalance();
        userWallet.updateBalance(userCurrent.add(delta));
        userWallet.updateLastUsedAt();
    }

    private void validatePositive(Long value, String fieldName) {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException(fieldName + " must be positive");
        }
    }
}

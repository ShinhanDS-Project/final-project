package com.merge.final_project.blockchain.service;

import com.merge.final_project.blockchain.entity.Key;
import com.merge.final_project.blockchain.entity.Transaction;
import com.merge.final_project.blockchain.entity.TransactionEventType;
import com.merge.final_project.blockchain.entity.TransactionStatus;
import com.merge.final_project.blockchain.repository.KeyRepository;
import com.merge.final_project.blockchain.repository.TransactionRepository;
import com.merge.final_project.campaign.campaigns.entity.Campaign;
import com.merge.final_project.campaign.campaigns.repository.CampaignRepository;
import com.merge.final_project.campaign.campaigns.CampaignStatus;
import com.merge.final_project.campaign.settlement.Repository.SettlementRepository;
import com.merge.final_project.campaign.settlement.Settlement;
import com.merge.final_project.campaign.settlement.SettlementStatus;
import com.merge.final_project.campaign.settlement.service.SettlementCommandService;
import com.merge.final_project.org.Foundation;
import com.merge.final_project.org.FoundationRepository;
import com.merge.final_project.recipient.beneficiary.entity.Beneficiary;
import com.merge.final_project.recipient.beneficiary.repository.BeneficiaryRepository;
import com.merge.final_project.wallet.entity.Wallet;
import com.merge.final_project.wallet.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.web3j.protocol.core.methods.response.TransactionReceipt;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SettlementTransactionService {

    private final WalletRepository walletRepository;
    private final FoundationRepository foundationRepository;
    private final BeneficiaryRepository beneficiaryRepository;
    private final KeyRepository keyRepository;
    private final BlockchainService blockchainService;
    private final TransactionRepository transactionRepository;
    private final SettlementCommandService settlementCommandService;
    private final SettlementRepository settlementRepository;
    private final CampaignRepository campaignRepository;

    @Transactional
    public void processSettlement(Campaign campaign) {
        LocalDateTime now = LocalDateTime.now();

        Campaign managedCampaign = campaignRepository.findById(campaign.getCampaignNo())
                .orElseThrow(() -> new IllegalArgumentException("캠페인을 찾을 수 없습니다."));

        if (managedCampaign.getCampaignStatus() != CampaignStatus.ENDED) {
            return;
        }

        // 이미 같은 캠페인에 대해 정산이 진행 중이거나 완료된 경우
        // 스케줄러가 다시 집더라도 중복 정산이 일어나지 않도록 바로 종료한다.
        if (settlementRepository.existsByCampaignAndStatusIn(
                managedCampaign,
                List.of(SettlementStatus.PENDING, SettlementStatus.COMPLETED)
        )) {
            return;
        }

        Wallet campaignWallet = walletRepository.findById(managedCampaign.getWalletNo())
                .orElseThrow(() -> new IllegalArgumentException("캠페인 지갑을 찾을 수 없습니다."));

        if (campaignWallet.getBalance() == null || campaignWallet.getBalance().compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }

        Foundation foundation = foundationRepository.findById(managedCampaign.getFoundationNo())
                .orElseThrow(() -> new IllegalArgumentException("재단을 찾을 수 없습니다."));

        if (foundation.getWallet() == null) {
            throw new IllegalArgumentException("재단 지갑이 없습니다.");
        }
        Wallet foundationWallet = foundation.getWallet();

        Beneficiary beneficiary = beneficiaryRepository.findById(managedCampaign.getBeneficiaryNo())
                .orElseThrow(() -> new IllegalArgumentException("수혜자를 찾을 수 없습니다."));

        Wallet beneficiaryWallet = beneficiary.getWallet();
        if (beneficiaryWallet == null) {
            throw new IllegalArgumentException("수혜자 지갑이 없습니다.");
        }

        if (campaignWallet.getKey() == null) {
            throw new IllegalArgumentException("캠페인 지갑 키가 없습니다.");
        }
        Key key = keyRepository.findById(campaignWallet.getKey().getKeyNo())
                .orElseThrow(() -> new IllegalArgumentException("캠페인 키를 찾을 수 없습니다."));

        BigDecimal total = campaignWallet.getBalance();

        if (foundation.getFeeRate() == null) {
            throw new IllegalArgumentException("재단 수수료율이 없습니다.");
        }

        BigDecimal feeRatePercent = foundation.getFeeRate();
        BigDecimal foundationAmount = total.multiply(feeRatePercent)
                .divide(BigDecimal.valueOf(100), 0, RoundingMode.DOWN);
        BigDecimal beneficiaryAmount = total.subtract(foundationAmount);

        String transactionCode = UUID.randomUUID().toString();

        // 온체인 호출 전에 정산 레코드를 먼저 PENDING 으로 생성한다.
        // 이렇게 해두면 체인 호출 실패 시 어떤 정산이 실패했는지 DB에 남길 수 있고,
        // 이후 재시도 여부를 판단할 기준도 확보할 수 있다.
        Settlement settlement = settlementCommandService.createPendingSettlement(
                transactionCode,
                foundation,
                beneficiary,
                total.intValue(),
                foundationAmount.intValue(),
                beneficiaryAmount.intValue(),
                managedCampaign
        );

        TransactionReceipt receipt;
        try {
            // 실제 스마트컨트랙트 정산 호출 구간이다.
            // 이 단계에서 예외가 나면 아직 온체인 정산이 완료되지 않은 것이므로
            // settlement 를 FAILED 로 바꿔 다음 배치/재시도 대상이 되게 한다.
            BigInteger feeBps = feeRatePercent
                    .multiply(BigDecimal.valueOf(100))
                    .toBigIntegerExact();

            receipt = blockchainService.settleCampaignOnChain(
                    key.getPrivateKey(),
                    foundationWallet.getWalletAddress(),
                    beneficiaryWallet.getWalletAddress(),
                    total.toBigInteger(),
                    feeBps,
                    BigInteger.valueOf(managedCampaign.getCampaignNo()),
                    BigInteger.valueOf(settlement.getSettlementNo())
            );
        } catch (Exception e) {
            settlementCommandService.markFailed(settlement.getSettlementNo());
            throw new RuntimeException("정산 온체인 처리에 실패했습니다.", e);
        }

        try {
            // 온체인 정산이 성공했으면 영수증(txHash, blockNumber, gasUsed)을 기준으로
            // 재단/수혜자 분배 내역과 캠페인 상태를 DB에 반영한다.
            // 이 후처리 단계에서 예외가 나더라도 체인 정산 자체는 이미 끝난 상태일 수 있으므로
            // settlement 를 FAILED 로 바꾸지 않고 예외만 올려 중복 온체인 호출을 피한다.
            String txHash = receipt.getTransactionHash();
            Long blockNum = receipt.getBlockNumber() != null ? receipt.getBlockNumber().longValue() : null;
            BigDecimal gasFee = receipt.getGasUsed() != null
                    ? BigDecimal.valueOf(receipt.getGasUsed().longValue())
                    : null;

            transactionRepository.save(
                    Transaction.builder()
                            .transactionCode(transactionCode)
                            .fromWallet(campaignWallet)
                            .toWallet(foundationWallet)
                            .amount(foundationAmount.intValue())
                            .sentAt(now)
                            .txHash(txHash)
                            .blockNum(blockNum)
                            .status(TransactionStatus.SUCCESS)
                            .gasFee(gasFee)
                            .eventType(TransactionEventType.SETTLEMENT_FEE)
                            .createdAt(now)
                            .build()
            );

            transactionRepository.save(
                    Transaction.builder()
                            .transactionCode(transactionCode)
                            .fromWallet(campaignWallet)
                            .toWallet(beneficiaryWallet)
                            .amount(beneficiaryAmount.intValue())
                            .sentAt(now)
                            .txHash(txHash)
                            .blockNum(blockNum)
                            .status(TransactionStatus.SUCCESS)
                            .gasFee(gasFee)
                            .eventType(TransactionEventType.SETTLEMENT_BENEFICIARY)
                            .createdAt(now)
                            .build()
            );

            managedCampaign.setCampaignStatus(CampaignStatus.SETTLED);
            campaignWallet.setBalance(BigDecimal.ZERO);
            settlementCommandService.markCompleted(settlement.getSettlementNo());
        } catch (Exception e) {
            throw new RuntimeException("정산 후처리 저장에 실패했습니다.", e);
        }
    }
}

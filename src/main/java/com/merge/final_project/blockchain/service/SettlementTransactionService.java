package com.merge.final_project.blockchain.service;

import com.merge.final_project.blockchain.entity.Key;
import com.merge.final_project.blockchain.entity.Transaction;
import com.merge.final_project.blockchain.entity.TransactionEventType;
import com.merge.final_project.blockchain.entity.TransactionStatus;
import com.merge.final_project.blockchain.repository.KeyRepository;
import com.merge.final_project.blockchain.repository.TransactionRepository;
import com.merge.final_project.campaign.campaigns.CampaignStatus;
import com.merge.final_project.campaign.campaigns.entity.Campaign;
import com.merge.final_project.campaign.campaigns.repository.CampaignRepository;
import com.merge.final_project.campaign.settlement.Repository.SettlementRepository;
import com.merge.final_project.campaign.settlement.Settlement;
import com.merge.final_project.campaign.settlement.SettlementStatus;
import com.merge.final_project.campaign.settlement.service.SettlementCommandService;
import com.merge.final_project.org.foundation.Foundation;
import com.merge.final_project.org.foundation.FoundationRepository;
import com.merge.final_project.recipient.beneficiary.Beneficiary;
import com.merge.final_project.recipient.beneficiary.BeneficiaryRepository;
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

        // 이미 같은 캠페인에 대해 정산 대기, 처리 중, 완료 이력이 있으면
        // 스케줄러가 다시 집어가더라도 중복 정산을 막기 위해 즉시 종료한다.
        if (settlementRepository.existsByCampaignAndStatusIn(
                managedCampaign,
                List.of(SettlementStatus.PENDING, SettlementStatus.PROCESSING, SettlementStatus.COMPLETED)
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

        // 온체인 정산 전에도 시도 이력을 남겨야
        // 체인 실패와 로컬 후처리 실패를 DB 에서 구분해서 볼 수 있다.
        Settlement settlement = settlementCommandService.createPendingSettlement(
                transactionCode,
                foundation,
                beneficiary,
                total.intValue(),
                foundationAmount.intValue(),
                beneficiaryAmount.intValue(),
                managedCampaign
        );

        // 실제 체인 호출에 들어가기 직전 PROCESSING 으로 바꿔둔다.
        // 이후 체인 성공 후 DB 반영만 실패해도 PENDING 과 구분할 수 있다.
        settlementCommandService.markProcessing(settlement.getSettlementNo());

        TransactionReceipt receipt;
        try {
            // 실제 스마트 컨트랙트 정산 호출 구간이다.
            // 여기서 예외가 나면 온체인 정산 자체가 실패한 것이므로 FAILED 로 바꾼다.
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
            // 온체인 정산이 성공한 뒤에만 영수증 값으로 로컬 트랜잭션을 남긴다.
            // 여기서 실패했다는 것은 체인은 이미 성공했고, 우리 DB 반영만 실패한 상태이므로
            // FAILED 로 바꾸지 않고 PROCESSING 으로 남겨 중복 정산을 막는다.
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
            // 캠페인 상태를 정산 완료 상태로 변경
            managedCampaign.setCampaignStatus(CampaignStatus.SETTLED);

            // 캠페인 지갑 온체인 잔액 조회 후 로컬 DB에 반영
            campaignWallet.updateBalance(
                    BigDecimal.valueOf(blockchainService.getTokenBalance(campaignWallet.getWalletAddress()).longValue())
            );
            // 지갑 사용 시간 갱신
            campaignWallet.updateLastUsedAt();

            // 기부단체 지갑 온체인 잔액 반영
            foundationWallet.updateBalance(
                    BigDecimal.valueOf(blockchainService.getTokenBalance(foundationWallet.getWalletAddress()).longValue())
            );
            foundationWallet.updateLastUsedAt();

            // 수혜자 지갑 온체인 잔액 반영
            beneficiaryWallet.updateBalance(
                    BigDecimal.valueOf(blockchainService.getTokenBalance(beneficiaryWallet.getWalletAddress()).longValue())
            );
            beneficiaryWallet.updateLastUsedAt();

            // 정산 상태를 COMPLETED로 변경
            settlementCommandService.markCompleted(settlement.getSettlementNo());
        } catch (Exception e) {
            throw new RuntimeException("정산 후처리에 실패했습니다.", e);
        }
    }
}

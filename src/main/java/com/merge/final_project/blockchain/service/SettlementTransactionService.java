package com.merge.final_project.blockchain.service;

import com.merge.final_project.blockchain.entity.Transaction;
import com.merge.final_project.blockchain.entity.TransactionEventType;
import com.merge.final_project.blockchain.entity.TransactionStatus;
import com.merge.final_project.blockchain.gas.GasStationService;
import com.merge.final_project.blockchain.repository.TransactionRepository;
import com.merge.final_project.blockchain.security.WalletPrivateKeyResolver;
import com.merge.final_project.campaign.campaigns.CampaignStatus;
import com.merge.final_project.campaign.campaigns.entity.Campaign;
import com.merge.final_project.campaign.campaigns.repository.CampaignRepository;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
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
@Slf4j
@RequiredArgsConstructor
public class SettlementTransactionService {

    private final WalletRepository walletRepository;
    private final FoundationRepository foundationRepository;
    private final BeneficiaryRepository beneficiaryRepository;
    private final BlockchainService blockchainService;
    private final TransactionRepository transactionRepository;
    private final SettlementCommandService settlementCommandService;
    private final SettlementRepository settlementRepository;
    private final CampaignRepository campaignRepository;
    private final WalletPrivateKeyResolver walletPrivateKeyResolver;
    private final TokenAmountConverter tokenAmountConverter;
    private final GasStationService gasStationService;

    /**
     * 캠페인 정산 처리 메인 로직
     *
     * [전체 흐름]
     * 1. 캠페인 상태 및 중복 정산 여부 체크
     * 2. 캠페인 지갑 잔액 및 온체인 잔액 검증
     * 3. 재단 / 수혜자 / 키 조회
     * 4. 정산 금액 계산
     * 5. Settlement 생성 (PENDING -> PROCESSING)
     * 6. 스마트컨트랙트 호출 (온체인 정산)
     * 7. 트랜잭션 저장 + 지갑 업데이트 + 상태 변경
     */
    @Transactional
    public void processSettlement(Campaign campaign) {
        // 현재 시각 (트랜잭션 기록용)
        LocalDateTime now = LocalDateTime.now();

        // 영속 상태로 다시 조회해 최신 캠페인 상태를 기준으로 정산한다.
        Campaign managedCampaign = campaignRepository.findById(campaign.getCampaignNo())
                .orElseThrow(() -> new IllegalArgumentException("campaign not found"));

        // 종료된 캠페인만 정산 대상이다.
        if (managedCampaign.getCampaignStatus() != CampaignStatus.ENDED) {
            return;
        }

        // 이미 대기/진행/완료 정산이 있으면 스케줄러 중복 실행을 막기 위해 종료한다.
        if (settlementRepository.existsByCampaignAndStatusIn(
                managedCampaign,
                List.of(
                        SettlementStatus.PENDING,
                        SettlementStatus.PROCESSING,
                        SettlementStatus.ONCHAIN_CONFIRMED,
                        SettlementStatus.COMPLETED
                )
        )) {
            return;
        }

        // 캠페인 지갑 조회
        Wallet campaignWallet = walletRepository.findById(managedCampaign.getWalletNo())
                .orElseThrow(() -> new IllegalArgumentException("campaign wallet not found"));

        // 로컬 잔액이 없으면 정산할 금액이 없다.
        if (campaignWallet.getBalance() == null || campaignWallet.getBalance().compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }

        BigDecimal onChainCampaignBalance;
        try {
            // DB 잔액만 믿지 않고 실제 체인 잔액을 다시 확인한다.
            onChainCampaignBalance = tokenAmountConverter.fromOnChainAmount(
                    blockchainService.getTokenBalance(campaignWallet.getWalletAddress())
            );
        } catch (Exception e) {
            throw new RuntimeException("failed to read campaign on-chain balance", e);
        }

        // 온체인 잔액이 비어 있거나 DB보다 적으면 정산을 중단한다.
        if (onChainCampaignBalance.compareTo(BigDecimal.ZERO) <= 0
                || onChainCampaignBalance.compareTo(campaignWallet.getBalance()) < 0) {
            log.warn(
                    "Skip settlement for campaignNo={}, walletAddress={}, dbBalance={}, onChainBalance={}",
                    managedCampaign.getCampaignNo(),
                    campaignWallet.getWalletAddress(),
                    campaignWallet.getBalance(),
                    onChainCampaignBalance
            );
            return;
        }

        // 기부단체 조회
        Foundation foundation = foundationRepository.findById(managedCampaign.getFoundationNo())
                .orElseThrow(() -> new IllegalArgumentException("foundation not found"));
        //기부단체 지갑
        if (foundation.getWallet() == null) {
            throw new IllegalArgumentException("foundation wallet not found");
        }
        Wallet foundationWallet = foundation.getWallet();

        // 수혜자 조회
        Beneficiary beneficiary = beneficiaryRepository.findById(managedCampaign.getBeneficiaryNo())
                .orElseThrow(() -> new IllegalArgumentException("beneficiary not found"));
        // 수혜자 지갑
        Wallet beneficiaryWallet = beneficiary.getWallet();
        if (beneficiaryWallet == null) {
            throw new IllegalArgumentException("beneficiary wallet not found");
        }

        // 캠페인 지갑과 연결된 개인키가 있어야 한다.
        if (campaignWallet.getKey() == null || campaignWallet.getKey().getKeyNo() == null) {
            throw new IllegalArgumentException("campaign wallet key not found");
        }
        // 정산 총액은 현재 캠페인 지갑 잔액이다.
        BigDecimal total = campaignWallet.getBalance();

        // 수수료율이 없으면 정산 금액 계산이 불가능
        BigDecimal feeRatePercent = normalizeFeeRatePercent(foundation.getFeeRate());

        // 기부단체 수수료 금액 계산
        // 소수점은 버림 처리하여 정수 단위 토큰으로 맞춘다.
        BigDecimal foundationAmount = total.multiply(feeRatePercent)
                .divide(BigDecimal.valueOf(100), 0, RoundingMode.DOWN);
        // 총액 - 수수료 = 수혜자 지급 금액
        BigDecimal beneficiaryAmount = total.subtract(foundationAmount);

        // 하나의 정산 작업을 묶어서 추적할 내부 거래 코드 생성
        String transactionCode = UUID.randomUUID().toString();

        Settlement settlement;
        try {
            // 온체인 호출 전에 정산 시도 이력을 먼저 남긴다.
            settlement = settlementCommandService.createPendingSettlement(
                    transactionCode,
                    foundation,
                    beneficiary,
                    toLongExact(total, "total amount"),
                    toLongExact(foundationAmount, "foundation amount"),
                    toLongExact(beneficiaryAmount, "beneficiary amount"),
                    managedCampaign
            );
        } catch (DataIntegrityViolationException e) {
            // 동시 실행 등으로 이미 정산이 생성된 경우 중복 생성하지 않는다.
            log.info("settlement already exists. campaignNo={}", managedCampaign.getCampaignNo());
            return;
        }

        // 체인 호출 직전 PROCESSING으로 전환해 PENDING과 구분한다.
        settlementCommandService.markProcessing(settlement.getSettlementNo());

        TransactionReceipt receipt;
        try {
            // 스마트컨트랙트는 수수료율을 bps(1%=100bp) 단위로 사용하므로 변환한다.
            BigInteger feeBps = feeRatePercent.movePointRight(2).toBigIntegerExact();
            gasStationService.ensureSufficientPol(campaignWallet);

            // 캠페인 지갑에서 기부단체/수혜자에게 토큰을 분배하는
            // 실제 온체인 정산 트랜잭션 실행
            receipt = blockchainService.settleCampaignOnChain(
                    walletPrivateKeyResolver.resolveForWallet(campaignWallet),
                    foundationWallet.getWalletAddress(),
                    beneficiaryWallet.getWalletAddress(),
                    tokenAmountConverter.toOnChainAmount(total),
                    feeBps,
                    BigInteger.valueOf(managedCampaign.getCampaignNo()),
                    BigInteger.valueOf(settlement.getSettlementNo())
            );
        } catch (Exception e) {
            // 온체인 자체가 실패한 경우이므로 정산 상태를 FAILED로 내린다.
            settlementCommandService.markFailed(settlement.getSettlementNo());
            throw new RuntimeException("failed to settle on chain", e);
        }

        // 체인 송금은 끝났지만 로컬 후처리가 아직 남아 있으므로 중간 상태로 남긴다.
        settlementCommandService.markOnChainConfirmed(settlement.getSettlementNo());

        try {
            // 체인 영수증을 기준으로 로컬 거래 2건(재단 몫 / 수혜자 몫)을 남긴다.
            String txHash = receipt.getTransactionHash();
            Long blockNum = receipt.getBlockNumber() != null ? receipt.getBlockNumber().longValue() : null;
            BigDecimal gasFee = receipt.getGasUsed() != null
                    ? BigDecimal.valueOf(receipt.getGasUsed().longValue())
                    : null;

            // 기부단체 수수료 지급 내역 저장
            transactionRepository.save(
                    Transaction.builder()
                            .transactionCode(transactionCode)
                            .fromWallet(campaignWallet)
                            .toWallet(foundationWallet)
                            .amount(toLongExact(foundationAmount, "settlement fee transaction amount"))
                            .sentAt(now)
                            .txHash(txHash)
                            .blockNum(blockNum)
                            .status(TransactionStatus.SUCCESS)
                            .gasFee(gasFee)
                            .eventType(TransactionEventType.SETTLEMENT_FEE)
                            .createdAt(now)
                            .build()
            );
            // 수혜자 정산금 지급 내역 저장
            transactionRepository.save(
                    Transaction.builder()
                            .transactionCode(transactionCode)
                            .fromWallet(campaignWallet)
                            .toWallet(beneficiaryWallet)
                            .amount(toLongExact(beneficiaryAmount, "beneficiary settlement transaction amount"))
                            .sentAt(now)
                            .txHash(txHash)
                            .blockNum(blockNum)
                            .status(TransactionStatus.SUCCESS)
                            .gasFee(gasFee)
                            .eventType(TransactionEventType.SETTLEMENT_BENEFICIARY)
                            .createdAt(now)
                            .build()
            );

            // 캠페인 상태를 최종 정산 완료 상태로 변경한다.
            managedCampaign.setCampaignStatus(CampaignStatus.SETTLED);

            // 정산 이후 각 지갑의 온체인 잔액을 다시 읽어 로컬 DB와 동기화한다.
            campaignWallet.updateBalance(
                    tokenAmountConverter.fromOnChainAmount(
                            blockchainService.getTokenBalance(campaignWallet.getWalletAddress())
                    )
            );
            campaignWallet.updateLastUsedAt();

            foundationWallet.updateBalance(
                    tokenAmountConverter.fromOnChainAmount(
                            blockchainService.getTokenBalance(foundationWallet.getWalletAddress())
                    )
            );
            foundationWallet.updateLastUsedAt();

            beneficiaryWallet.updateBalance(
                    tokenAmountConverter.fromOnChainAmount(
                            blockchainService.getTokenBalance(beneficiaryWallet.getWalletAddress())
                    )
            );
            beneficiaryWallet.updateLastUsedAt();

            // 모든 후처리까지 정상 종료되면 정산 상태를 완료로 변경한다.
            settlementCommandService.markCompleted(settlement.getSettlementNo());
        } catch (Exception e) {
            throw new RuntimeException("failed to finalize settlement", e);
        }
    }

    // BigDecimal -> Long 변환 (정수 + 범위 검증)
    private Long toLongExact(BigDecimal amount, String fieldName) {
        try {
            return amount.longValueExact();
        } catch (ArithmeticException e) {
            throw new IllegalArgumentException(fieldName + " must fit in long and be an integer", e);
        }
    }

    private BigDecimal normalizeFeeRatePercent(BigDecimal feeRate) {
        if (feeRate == null) {
            throw new IllegalArgumentException("foundation fee rate not found");
        }

        BigDecimal feeRatePercent = feeRate.compareTo(BigDecimal.ONE) <= 0
                ? feeRate.movePointRight(2)
                : feeRate;

        if (feeRatePercent.compareTo(BigDecimal.ZERO) < 0 || feeRatePercent.compareTo(BigDecimal.valueOf(100)) > 0) {
            throw new IllegalArgumentException("invalid foundation fee rate: " + feeRate);
        }
        if (feeRatePercent.scale() > 2) {
            throw new IllegalArgumentException("invalid foundation fee rate precision: " + feeRate);
        }
        return feeRatePercent.stripTrailingZeros();
    }
}

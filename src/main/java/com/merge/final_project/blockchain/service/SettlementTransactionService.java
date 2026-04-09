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
import com.merge.final_project.wallet.entity.WalletRepository;
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
    private final KeyRepository keyRepository;
    private final BlockchainService blockchainService;
    private final TransactionRepository transactionRepository;
    private final SettlementCommandService settlementCommandService;
    private final SettlementRepository settlementRepository;
    private final CampaignRepository campaignRepository;

    /**
     * 캠페인 정산 처리 메인 로직
     *
     * [전체 흐름]
     * 1. 캠페인 상태 및 중복 정산 여부 체크
     * 2. 캠페인 지갑 잔액 및 온체인 잔액 검증
     * 3. 재단 / 수혜자 / 키 조회
     * 4. 정산 금액 계산
     * 5. Settlement 생성 (PENDING → PROCESSING)
     * 6. 스마트컨트랙트 호출 (온체인 정산)
     * 7. 트랜잭션 저장 + 지갑 업데이트 + 상태 변경
     */

    @Transactional
    public void processSettlement(Campaign campaign) {
        // 현재 시각 (트랜잭션 기록용)
        LocalDateTime now = LocalDateTime.now();

        // ===================== 1. 캠페인 조회 =====================
        // 영속 상태로 다시 조회 (Lazy 문제 및 최신 상태 보장)
        Campaign managedCampaign = campaignRepository.findById(campaign.getCampaignNo())
                .orElseThrow(() -> new IllegalArgumentException("캠페인을 찾을 수 없습니다."));

        // 캠페인이 종료 상태가 아니면 정산하지 않음
        if (managedCampaign.getCampaignStatus() != CampaignStatus.ENDED) {
            return;
        }

        // ===================== 2. 중복 정산 방지 =====================
        // 이미 같은 캠페인에 대해 정산 대기, 처리 중, 완료 이력이 있으면 스케줄러가 다시 집어가더라도 중복 정산을 막기 위해 즉시 종료한다.
        if (settlementRepository.existsByCampaignAndStatusIn(
                managedCampaign,
                List.of(SettlementStatus.PENDING, SettlementStatus.PROCESSING, SettlementStatus.COMPLETED)
        )) {
            return;
        }

        // ===================== 3. 캠페인 지갑 조회 =====================
        Wallet campaignWallet = walletRepository.findById(managedCampaign.getWalletNo())
                .orElseThrow(() -> new IllegalArgumentException("캠페인 지갑을 찾을 수 없습니다."));

        // 지갑 잔액이 없으면 정산할 필요 없음
        if (campaignWallet.getBalance() == null || campaignWallet.getBalance().compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }

        // ===================== 4. 온체인 잔액 검증 =====================
        BigDecimal onChainCampaignBalance;
        try {
            // 블록체인에서 실제 토큰 잔액 조회
            onChainCampaignBalance = new BigDecimal(
                    blockchainService.getTokenBalance(campaignWallet.getWalletAddress())
            );
        } catch (Exception e) {
            throw new RuntimeException("캠페인 지갑 온체인 잔액 조회에 실패했습니다.", e);
        }
        // DB 잔액과 온체인 잔액 불일치 또는 부족 시 정산 중단
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

        // ===================== 5. 재단 조회 =====================
        Foundation foundation = foundationRepository.findById(managedCampaign.getFoundationNo())
                .orElseThrow(() -> new IllegalArgumentException("재단을 찾을 수 없습니다."));

        if (foundation.getWallet() == null) {
            throw new IllegalArgumentException("재단 지갑이 없습니다.");
        }
        Wallet foundationWallet = foundation.getWallet();

        // ===================== 6. 수혜자 조회 =====================
        Beneficiary beneficiary = beneficiaryRepository.findById(managedCampaign.getBeneficiaryNo())
                .orElseThrow(() -> new IllegalArgumentException("수혜자를 찾을 수 없습니다."));

        Wallet beneficiaryWallet = beneficiary.getWallet();
        if (beneficiaryWallet == null) {
            throw new IllegalArgumentException("수혜자 지갑이 없습니다.");
        }

        // ===================== 7. 캠페인 키 조회 =====================
        if (campaignWallet.getKey() == null) {
            throw new IllegalArgumentException("캠페인 지갑 키가 없습니다.");
        }
        Key key = keyRepository.findById(campaignWallet.getKey().getKeyNo())
                .orElseThrow(() -> new IllegalArgumentException("캠페인 키를 찾을 수 없습니다."));

        // ===================== 8. 정산 금액 계산 =====================
        BigDecimal total = campaignWallet.getBalance();

        if (foundation.getFeeRate() == null) {
            throw new IllegalArgumentException("재단 수수료율이 없습니다.");
        }

        // 재단 수수료 계산, 수수료를 퍼센트 기준으로 통일 (0.xx → ×100, 이미 %면 그대로 사용)
        BigDecimal feeRatePercent = foundation.getFeeRate().compareTo(BigDecimal.ONE) < 0
                ? foundation.getFeeRate().multiply(BigDecimal.valueOf(100))
                : foundation.getFeeRate();
        // 재단 몫
        BigDecimal foundationAmount = total.multiply(feeRatePercent)
                .divide(BigDecimal.valueOf(100), 0, RoundingMode.DOWN);
        // 수혜자 몫
        BigDecimal beneficiaryAmount = total.subtract(foundationAmount);

        // 트랜잭션 식별 코드 (DB 트랜잭션 묶음용)
        String transactionCode = UUID.randomUUID().toString();

        // 온체인 정산 전에도 시도 이력을 남겨야
        // 체인 실패와 로컬 후처리 실패를 DB 에서 구분해서 볼 수 있다.
        Settlement settlement;
        try {
            settlement = settlementCommandService.createPendingSettlement(
                    transactionCode,
                    foundation,
                    beneficiary,
                    toLongExact(total, "정산 총액"),
                    toLongExact(foundationAmount, "재단 정산 금액"),
                    toLongExact(beneficiaryAmount, "수혜자 정산 금액"),
                    managedCampaign
            );
            //진행중인 정산 디비가 있음
        } catch (DataIntegrityViolationException e) {
            log.info("이미 정산이 생성됨 (중복 요청) campaignNo={}", managedCampaign.getCampaignNo());
            return;
        }

        // 상태를 PROCESSING으로 변경 (온체인 실행 직전)
        settlementCommandService.markProcessing(settlement.getSettlementNo());

        // ===================== 10. 온체인 정산 =====================
        TransactionReceipt receipt;
        try {
            // 스마트 컨트랙트는 BPS 단위 (basis point) 사용
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
            // 온체인 실패 → 정산 실패 처리
            settlementCommandService.markFailed(settlement.getSettlementNo());
            throw new RuntimeException("정산 온체인 처리에 실패했습니다.", e);
        }

        // ===================== 11. 후처리 (DB 반영) =====================
        try {
            // 온체인 정산이 성공한 뒤에만 영수증 값으로 로컬 트랜잭션을 남긴다.
            // 여기서 실패했다는 것은 체인은 이미 성공했고, 우리 DB 반영만 실패한 상태이므로
            // FAILED 로 바꾸지 않고 PROCESSING 으로 남겨 중복 정산을 막는다.
            String txHash = receipt.getTransactionHash();
            Long blockNum = receipt.getBlockNumber() != null ? receipt.getBlockNumber().longValue() : null;
            // 가스비 저장
            BigDecimal gasFee = receipt.getGasUsed() != null
                    ? BigDecimal.valueOf(receipt.getGasUsed().longValue())
                    : null;

            // 재단 트랜잭션 저장
            transactionRepository.save(
                    Transaction.builder()
                            .transactionCode(transactionCode)
                            .fromWallet(campaignWallet)
                            .toWallet(foundationWallet)
                            .amount(toLongExact(foundationAmount, "재단 정산 트랜잭션 금액"))
                            .sentAt(now)
                            .txHash(txHash)
                            .blockNum(blockNum)
                            .status(TransactionStatus.SUCCESS)
                            .gasFee(gasFee)
                            .eventType(TransactionEventType.SETTLEMENT_FEE)
                            .createdAt(now)
                            .build()
            );
            // 수혜자 트랜잭션 저장
            transactionRepository.save(
                    Transaction.builder()
                            .transactionCode(transactionCode)
                            .fromWallet(campaignWallet)
                            .toWallet(beneficiaryWallet)
                            .amount(toLongExact(beneficiaryAmount, "수혜자 정산 트랜잭션 금액"))
                            .sentAt(now)
                            .txHash(txHash)
                            .blockNum(blockNum)
                            .status(TransactionStatus.SUCCESS)
                            .gasFee(gasFee)
                            .eventType(TransactionEventType.SETTLEMENT_BENEFICIARY)
                            .createdAt(now)
                            .build()
            );
            // 캠페인 상태 변경 (정산 완료)
            managedCampaign.setCampaignStatus(CampaignStatus.SETTLED);

            // ===================== 12. 지갑 동기화 =====================
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

            // ===================== 13. 정산 완료 =====================
            settlementCommandService.markCompleted(settlement.getSettlementNo());
        } catch (Exception e) {
            throw new RuntimeException("정산 후처리에 실패했습니다.", e);
        }
    }

    // BigDecimal → Long 변환 (정수 + 범위 검증)
    // 소수점이 있거나 long 범위를 벗어나면 예외 발생
    private Long toLongExact(BigDecimal amount, String fieldName) {
        try {
            return amount.longValueExact();
        } catch (ArithmeticException e) {
            throw new IllegalArgumentException(fieldName + "이 long 범위를 벗어나거나 정수가 아닙니다.", e);
        }
    }
}

package com.merge.final_project.blockchain.service;

import com.merge.final_project.blockchain.dto.BlockchainPageInfoResponse;
import com.merge.final_project.blockchain.dto.BlockchainSearchResolveResponse;
import com.merge.final_project.blockchain.dto.BlockchainSummaryResponse;
import com.merge.final_project.blockchain.dto.BlockchainTransactionDetailResponse;
import com.merge.final_project.blockchain.dto.BlockchainTransactionItemResponse;
import com.merge.final_project.blockchain.dto.BlockchainTransactionsResponse;
import com.merge.final_project.blockchain.dto.BlockchainWalletDetailResponse;
import com.merge.final_project.blockchain.dto.BlockchainWalletSummaryResponse;
import com.merge.final_project.blockchain.entity.Transaction;
import com.merge.final_project.blockchain.entity.TransactionEventType;
import com.merge.final_project.blockchain.entity.TransactionStatus;
import com.merge.final_project.blockchain.repository.TransactionRepository;
import com.merge.final_project.campaign.campaigns.entity.Campaign;
import com.merge.final_project.campaign.campaigns.repository.CampaignRepository;
import com.merge.final_project.org.Foundation;
import com.merge.final_project.org.FoundationRepository;
import com.merge.final_project.recipient.beneficiary.entity.Beneficiary;
import com.merge.final_project.recipient.beneficiary.repository.BeneficiaryRepository;
import com.merge.final_project.user.users.User;
import com.merge.final_project.user.users.UserRepository;
import com.merge.final_project.wallet.entity.Wallet;
import com.merge.final_project.wallet.entity.WalletType;
import com.merge.final_project.wallet.repository.WalletLookupRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BlockchainDashboardQueryService {

    private static final int DASHBOARD_PAGE_SIZE = 10;
    private static final int WALLET_DETAIL_PAGE_SIZE = 5;

    private final TransactionRepository transactionRepository;
    private final WalletLookupRepository walletLookupRepository;
    private final CampaignRepository campaignRepository;
    private final FoundationRepository foundationRepository;
    private final UserRepository userRepository;
    private final BeneficiaryRepository beneficiaryRepository;
    private final TokenAmountConverter tokenAmountConverter;

    public BlockchainTransactionsResponse getTransactions(int page, int size, String keyword, String statusText) {
        TransactionStatus status = resolveStatus(statusText);
        LookupCache cache = new LookupCache();

        Page<Transaction> transactionPage = loadTransactionPage(page, size, status, keyword);
        List<BlockchainTransactionItemResponse> items = transactionPage.getContent().stream()
                .map(transaction -> toTransactionItem(transaction, cache))
                .toList();

        return new BlockchainTransactionsResponse(items, toPageInfo(transactionPage));
    }

    public BlockchainSummaryResponse getSummary(int page, String statusText) {
        TransactionStatus status = resolveStatus(statusText);
        Page<Transaction> transactionPage = transactionRepository.findByStatus(
                status,
                createDashboardPageable(page, DASHBOARD_PAGE_SIZE)
        );

        long totalGapSec = 0L;
        int gapCount = 0;
        List<Transaction> pageItems = transactionPage.getContent();
        for (int i = 0; i < pageItems.size() - 1; i++) {
            LocalDateTime current = resolveOccurredAt(pageItems.get(i));
            LocalDateTime next = resolveOccurredAt(pageItems.get(i + 1));
            if (current == null || next == null) {
                continue;
            }
            totalGapSec += Math.abs(Duration.between(next, current).getSeconds());
            gapCount++;
        }

        BigDecimal avgBlockTimeSec = gapCount == 0
                ? BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.valueOf(totalGapSec)
                .divide(BigDecimal.valueOf(gapCount), 2, RoundingMode.HALF_UP);

        Long latestBlock = transactionRepository.findTopByStatusAndBlockNumIsNotNullOrderByBlockNumDesc(status)
                .map(Transaction::getBlockNum)
                .orElse(null);

        long totalTx = transactionRepository.countByStatus(status);
        Long tokenAmount = transactionRepository.sumAmountByStatusAndEventTypes(
                status,
                List.of(
                        TransactionEventType.ALLOCATION,
                        TransactionEventType.PAYMENT_TOKEN_CHARGE,
                        TransactionEventType.TokenAllocated
                )
        );

        return new BlockchainSummaryResponse(
                latestBlock,
                avgBlockTimeSec,
                totalTx,
                tokenAmount == null ? 0L : tokenAmount
        );
    }

    public BlockchainTransactionDetailResponse getTransactionDetail(String txHash, String statusText) {
        TransactionStatus status = resolveStatus(statusText);
        Transaction transaction = transactionRepository
                .findTopByTxHashIgnoreCaseAndStatusOrderByTransactionNoDesc(txHash, status)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "transaction not found: " + txHash));

        LookupCache cache = new LookupCache();
        BlockchainTransactionItemResponse item = toTransactionItem(transaction, cache);

        return new BlockchainTransactionDetailResponse(
                item.transactionCode(),
                item.txHash(),
                item.blockNum(),
                item.status(),
                item.eventType(),
                item.eventTypeLabel(),
                item.amount(),
                item.sentAt(),
                item.gasFee(),
                item.foundationName(),
                item.campaignName(),
                item.memo(),
                item.fromOwnerTypeLabel(),
                item.toOwnerTypeLabel(),
                toWalletSummary(transaction.getFromWallet(), cache),
                toWalletSummary(transaction.getToWallet(), cache)
        );
    }

    public BlockchainWalletDetailResponse getWalletDetail(String walletAddress, int page, String statusText) {
        TransactionStatus status = resolveStatus(statusText);
        Wallet wallet = walletLookupRepository.findByWalletAddressIgnoreCase(walletAddress)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "wallet not found: " + walletAddress));

        LookupCache cache = new LookupCache();
        Page<Transaction> transactionPage = transactionRepository.findPageByWalletNoAndStatus(
                wallet.getWalletNo(),
                status,
                createWalletDetailPageable(page)
        );
        List<BlockchainTransactionItemResponse> items = transactionPage.getContent().stream()
                .map(transaction -> toTransactionItem(transaction, cache))
                .toList();

        return new BlockchainWalletDetailResponse(
                toWalletSummary(wallet, cache),
                items,
                toPageInfo(transactionPage)
        );
    }

    public BlockchainSearchResolveResponse resolveSearchTarget(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return new BlockchainSearchResolveResponse("not_found", null);
        }

        String normalized = keyword.trim();
        TransactionStatus success = TransactionStatus.SUCCESS;

        if (transactionRepository.findTopByTxHashIgnoreCaseAndStatusOrderByTransactionNoDesc(normalized, success).isPresent()) {
            return new BlockchainSearchResolveResponse("transaction", normalized);
        }

        if (walletLookupRepository.findByWalletAddressIgnoreCase(normalized).isPresent()) {
            return new BlockchainSearchResolveResponse("wallet", normalized);
        }

        return foundationRepository.findFirstByFoundationNameIgnoreCase(normalized)
                .flatMap(foundation -> walletLookupRepository.findByWalletTypeAndOwnerNo(WalletType.FOUNDATION, foundation.getFoundationNo()))
                .map(wallet -> new BlockchainSearchResolveResponse("wallet", wallet.getWalletAddress()))
                .or(() -> campaignRepository.findFirstByTitleIgnoreCase(normalized)
                        .flatMap(campaign -> {
                            if (campaign.getWalletNo() == null) {
                                return java.util.Optional.empty();
                            }
                            return walletLookupRepository.findById(campaign.getWalletNo());
                        })
                        .map(wallet -> new BlockchainSearchResolveResponse("wallet", wallet.getWalletAddress())))
                .orElseGet(() -> new BlockchainSearchResolveResponse("not_found", null));
    }

    private BlockchainTransactionItemResponse toTransactionItem(Transaction transaction, LookupCache cache) {
        Wallet fromWallet = transaction.getFromWallet();
        Wallet toWallet = transaction.getToWallet();

        Names names = resolveNamesForTransaction(fromWallet, toWallet, cache);

        return new BlockchainTransactionItemResponse(
                transaction.getTransactionCode(),
                transaction.getTxHash(),
                transaction.getBlockNum(),
                transaction.getStatus() == null ? null : transaction.getStatus().name(),
                toApiEventType(transaction.getEventType()),
                toEventTypeLabel(transaction.getEventType()),
                resolveDisplayAmount(transaction),
                transaction.getGasFee(),
                resolveOccurredAt(transaction),
                fromWallet == null ? null : fromWallet.getWalletAddress(),
                toWallet == null ? null : toWallet.getWalletAddress(),
                names.foundationName(),
                names.campaignName(),
                toMemo(transaction.getEventType()),
                toOwnerTypeLabel(fromWallet),
                toOwnerTypeLabel(toWallet)
        );
    }

    private BlockchainWalletSummaryResponse toWalletSummary(Wallet wallet, LookupCache cache) {
        if (wallet == null) {
            return null;
        }

        Names names = resolveNamesForWallet(wallet, cache);
        return new BlockchainWalletSummaryResponse(
                wallet.getWalletAddress(),
                names.foundationName(),
                names.campaignName(),
                wallet.getBalance() == null ? BigDecimal.ZERO : wallet.getBalance(),
                toOwnerType(wallet),
                toOwnerTypeLabel(wallet)
        );
    }

    private Names resolveNamesForTransaction(Wallet fromWallet, Wallet toWallet, LookupCache cache) {
        Wallet campaignWallet = walletByType(fromWallet, toWallet, WalletType.CAMPAIGN);
        if (campaignWallet != null) {
            return resolveNamesForWallet(campaignWallet, cache);
        }

        Wallet foundationWallet = walletByType(fromWallet, toWallet, WalletType.FOUNDATION);
        if (foundationWallet != null) {
            return resolveNamesForWallet(foundationWallet, cache);
        }

        return new Names(null, null);
    }

    private Wallet walletByType(Wallet fromWallet, Wallet toWallet, WalletType walletType) {
        if (fromWallet != null && fromWallet.getWalletType() == walletType) {
            return fromWallet;
        }
        if (toWallet != null && toWallet.getWalletType() == walletType) {
            return toWallet;
        }
        return null;
    }

    private Names resolveNamesForWallet(Wallet wallet, LookupCache cache) {
        if (wallet == null || wallet.getWalletType() == null) {
            return new Names(null, null);
        }

        return switch (wallet.getWalletType()) {
            case FOUNDATION -> new Names(resolveFoundationName(wallet.getOwnerNo(), cache), null);
            case CAMPAIGN -> {
                Campaign campaign = resolveCampaignByWalletNo(wallet.getWalletNo(), cache);
                if (campaign == null) {
                    yield new Names(null, null);
                }
                String foundationName = resolveFoundationName(campaign.getFoundationNo(), cache);
                yield new Names(foundationName, campaign.getTitle());
            }
            case USER -> new Names(resolveUserName(wallet.getOwnerNo(), cache), null);
            case BENEFICIARY -> new Names(resolveBeneficiaryName(wallet.getOwnerNo(), cache), null);
            case HOT, COLD, SERVER -> new Names("플랫폼 서버", "시스템 운영");
        };
    }

    private String resolveFoundationName(Long foundationNo, LookupCache cache) {
        if (foundationNo == null) {
            return null;
        }
        Foundation foundation = cache.foundationByNo.computeIfAbsent(
                foundationNo,
                key -> foundationRepository.findById(key).orElse(null)
        );
        return foundation == null ? null : foundation.getFoundationName();
    }

    private Campaign resolveCampaignByWalletNo(Long walletNo, LookupCache cache) {
        if (walletNo == null) {
            return null;
        }
        return cache.campaignByWalletNo.computeIfAbsent(
                walletNo,
                key -> campaignRepository.findByWalletNo(key).orElse(null)
        );
    }

    private String resolveUserName(Long userNo, LookupCache cache) {
        if (userNo == null) {
            return null;
        }
        User user = cache.userByNo.computeIfAbsent(
                userNo,
                key -> userRepository.findById(key).orElse(null)
        );
        return user == null ? "기부자" : user.getName();
    }

    private String resolveBeneficiaryName(Long beneficiaryNo, LookupCache cache) {
        if (beneficiaryNo == null) {
            return null;
        }
        Beneficiary beneficiary = cache.beneficiaryByNo.computeIfAbsent(
                beneficiaryNo,
                key -> beneficiaryRepository.findById(key).orElse(null)
        );
        return beneficiary == null ? "수혜자" : beneficiary.getName();
    }

    private TransactionStatus resolveStatus(String statusText) {
        if (statusText == null || statusText.isBlank()) {
            return TransactionStatus.SUCCESS;
        }
        try {
            return TransactionStatus.valueOf(statusText.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "invalid status: " + statusText);
        }
    }

    private String toApiEventType(TransactionEventType eventType) {
        if (eventType == null) {
            return "UNKNOWN";
        }
        return switch (eventType) {
            case DONATION, DONATION_TRANSFER -> "DONATION";
            case ALLOCATION, PAYMENT_TOKEN_CHARGE, TokenAllocated -> "TOKENIZATION";
            case SETTLEMENT_FEE, SETTLEMENT_BENEFICIARY -> "SETTLEMENT";
            case REDEMPTION -> "CASHOUT";
            case POL_AUTO_TOPUP -> "GAS_TOPUP";
        };
    }

    private String toEventTypeLabel(TransactionEventType eventType) {
        if (eventType == null) {
            return "미확인";
        }
        return switch (eventType) {
            case DONATION, DONATION_TRANSFER -> "기부";
            case ALLOCATION, PAYMENT_TOKEN_CHARGE, TokenAllocated -> "토큰화";
            case SETTLEMENT_FEE, SETTLEMENT_BENEFICIARY -> "정산";
            case REDEMPTION -> "현금화";
            case POL_AUTO_TOPUP -> "가스충전";
        };
    }

    private String toMemo(TransactionEventType eventType) {
        if (eventType == null) {
            return null;
        }
        return switch (eventType) {
            case DONATION, DONATION_TRANSFER -> "기부자 후원 전송";
            case ALLOCATION, PAYMENT_TOKEN_CHARGE, TokenAllocated -> "기부자 토큰화 지급";
            case SETTLEMENT_FEE, SETTLEMENT_BENEFICIARY -> "캠페인 정산 전송";
            case REDEMPTION -> "현금화 요청";
            case POL_AUTO_TOPUP -> "가스 자동 충전";
        };
    }

    private String toOwnerType(Wallet wallet) {
        if (wallet == null || wallet.getWalletType() == null) {
            return "UNKNOWN";
        }
        return switch (wallet.getWalletType()) {
            case USER -> "DONOR";
            case FOUNDATION -> "FOUNDATION";
            case CAMPAIGN -> "CAMPAIGN";
            case BENEFICIARY -> "BENEFICIARY";
            case HOT, COLD, SERVER -> "SERVER";
        };
    }

    private String toOwnerTypeLabel(Wallet wallet) {
        if (wallet == null || wallet.getWalletType() == null) {
            return "미확인";
        }
        return switch (wallet.getWalletType()) {
            case USER -> "기부자";
            case FOUNDATION -> "재단";
            case CAMPAIGN -> "캠페인";
            case BENEFICIARY -> "수혜자";
            case HOT, COLD, SERVER -> "서버";
        };
    }

    private LocalDateTime resolveOccurredAt(Transaction transaction) {
        return transaction.getSentAt() != null ? transaction.getSentAt() : transaction.getCreatedAt();
    }

    private BigDecimal resolveDisplayAmount(Transaction transaction) {
        if (transaction == null || transaction.getAmount() == null) {
            return null;
        }
        if (transaction.getEventType() == TransactionEventType.POL_AUTO_TOPUP) {
            return tokenAmountConverter.fromOnChainAmount(BigInteger.valueOf(transaction.getAmount()));
        }
        return BigDecimal.valueOf(transaction.getAmount());
    }

    private Page<Transaction> loadTransactionPage(int page, int size, TransactionStatus status, String keyword) {
        Pageable pageable = createDashboardPageable(page, size);
        String normalizedKeyword = normalizeKeyword(keyword);
        if (normalizedKeyword.isEmpty()) {
            return transactionRepository.findByStatus(status, pageable);
        }
        return transactionRepository.searchDashboardPage(status.name(), normalizedKeyword, pageable);
    }

    private Pageable createDashboardPageable(int page, int pageSize) {
        return createPageable(page, sanitizeDashboardPageSize(pageSize));
    }

    private Pageable createWalletDetailPageable(int page) {
        return createPageable(page, WALLET_DETAIL_PAGE_SIZE);
    }

    private Pageable createPageable(int page, int pageSize) {
        int safePage = Math.max(0, page - 1);
        return PageRequest.of(
                safePage,
                pageSize,
                Sort.by(Sort.Order.desc("sentAt"), Sort.Order.desc("transactionNo"))
        );
    }

    private int sanitizeDashboardPageSize(int pageSize) {
        if (pageSize <= 0) {
            return DASHBOARD_PAGE_SIZE;
        }
        return Math.min(pageSize, 100);
    }

    private String normalizeKeyword(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return "";
        }
        String escapedKeyword = keyword.trim()
                .toLowerCase(Locale.ROOT)
                .replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_");
        return "%" + escapedKeyword + "%";
    }

    private BlockchainPageInfoResponse toPageInfo(Page<?> page) {
        return new BlockchainPageInfoResponse(
                page.getNumber() + 1,
                page.getSize(),
                page.getTotalElements(),
                Math.max(1, page.getTotalPages())
        );
    }

    private record Names(String foundationName, String campaignName) {
    }

    private static class LookupCache {
        private final Map<Long, Campaign> campaignByWalletNo = new HashMap<>();
        private final Map<Long, Foundation> foundationByNo = new HashMap<>();
        private final Map<Long, User> userByNo = new HashMap<>();
        private final Map<Long, Beneficiary> beneficiaryByNo = new HashMap<>();
    }
}

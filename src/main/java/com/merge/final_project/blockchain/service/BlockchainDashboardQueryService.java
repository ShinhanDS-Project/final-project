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
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BlockchainDashboardQueryService {

    private static final int DEFAULT_PAGE_SIZE = 5;

    private final TransactionRepository transactionRepository;
    private final WalletLookupRepository walletLookupRepository;
    private final CampaignRepository campaignRepository;
    private final FoundationRepository foundationRepository;
    private final UserRepository userRepository;
    private final BeneficiaryRepository beneficiaryRepository;

    /**
     * 거래 목록 조회 API용 서비스 메서드.
     * 상태 필터 적용 후 화면용 DTO로 변환하고, 키워드 검색 + 페이징을 수행한다.
     */
    public BlockchainTransactionsResponse getTransactions(int page, String keyword, String statusText) {
        TransactionStatus status = resolveStatus(statusText);
        LookupCache cache = new LookupCache();

        List<Transaction> transactions = transactionRepository.findByStatusOrderBySentAtDescTransactionNoDesc(status);
        List<BlockchainTransactionItemResponse> mapped = new ArrayList<>(transactions.size());
        for (Transaction transaction : transactions) {
            BlockchainTransactionItemResponse item = toTransactionItem(transaction, cache);
            if (matchesKeyword(item, keyword)) {
                mapped.add(item);
            }
        }

        PageSlice<BlockchainTransactionItemResponse> slice = paginate(mapped, page, DEFAULT_PAGE_SIZE);
        return new BlockchainTransactionsResponse(slice.items(), slice.pageInfo());
    }

    /**
     * 대시보드 요약 데이터 조회.
     * 최신 블록/평균 블록 간격/총 거래 수/토큰 집계값을 계산한다.
     */
    public BlockchainSummaryResponse getSummary(int page, String statusText) {
        TransactionStatus status = resolveStatus(statusText);
        List<Transaction> transactions = transactionRepository.findByStatusOrderBySentAtDescTransactionNoDesc(status);
        PageSlice<Transaction> slice = paginate(transactions, page, DEFAULT_PAGE_SIZE);

        long totalGapSec = 0L;
        int gapCount = 0;
        List<Transaction> pageItems = slice.items();
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

    /**
     * txHash 기준 단건 상세 조회.
     * 거래가 없으면 404를 반환한다.
     */
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

    /**
     * 특정 지갑 상세 + 연관 거래 목록 조회.
     * from/to 양방향 거래를 함께 내려준다.
     */
    public BlockchainWalletDetailResponse getWalletDetail(String walletAddress, int page, String statusText) {
        TransactionStatus status = resolveStatus(statusText);
        Wallet wallet = walletLookupRepository.findByWalletAddressIgnoreCase(walletAddress)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "wallet not found: " + walletAddress));

        LookupCache cache = new LookupCache();
        List<Transaction> transactions = transactionRepository.findByWalletAddressAndStatus(walletAddress, status);
        List<BlockchainTransactionItemResponse> items = transactions.stream()
                .map(transaction -> toTransactionItem(transaction, cache))
                .toList();

        PageSlice<BlockchainTransactionItemResponse> slice = paginate(items, page, DEFAULT_PAGE_SIZE);
        return new BlockchainWalletDetailResponse(
                toWalletSummary(wallet, cache),
                slice.items(),
                slice.pageInfo()
        );
    }

    /**
     * 검색어를 실제 이동 대상으로 해석한다.
     * 우선순위: txHash -> walletAddress -> foundationName -> campaignTitle.
     */
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

    /**
     * Transaction 엔티티를 거래 리스트/상세 화면 공통 DTO로 변환한다.
     */
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
                transaction.getAmount(),
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

    /**
     * 지갑 엔티티를 화면 표시용 요약 DTO로 변환한다.
     */
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

    /**
     * 거래의 from/to 지갑 정보를 이용해 단체명/캠페인명 표시 대상을 결정한다.
     */
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

    /**
     * from/to 중 지정한 타입의 지갑을 반환한다.
     */
    private Wallet walletByType(Wallet fromWallet, Wallet toWallet, WalletType walletType) {
        if (fromWallet != null && fromWallet.getWalletType() == walletType) {
            return fromWallet;
        }
        if (toWallet != null && toWallet.getWalletType() == walletType) {
            return toWallet;
        }
        return null;
    }

    /**
     * 지갑 타입별 owner 이름 정보를 조회한다.
     */
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

    /**
     * foundationNo로 단체명을 조회한다. 동일 요청 내 조회 결과는 캐시한다.
     */
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

    /**
     * walletNo 기준 캠페인을 조회한다. 동일 요청 내 조회 결과는 캐시한다.
     */
    private Campaign resolveCampaignByWalletNo(Long walletNo, LookupCache cache) {
        if (walletNo == null) {
            return null;
        }
        return cache.campaignByWalletNo.computeIfAbsent(
                walletNo,
                key -> campaignRepository.findByWalletNo(key).orElse(null)
        );
    }

    /**
     * userNo로 기부자 이름을 조회한다. 동일 요청 내 조회 결과는 캐시한다.
     */
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

    /**
     * beneficiaryNo로 수혜자 이름을 조회한다. 동일 요청 내 조회 결과는 캐시한다.
     */
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

    /**
     * 거래 검색 키워드 매칭 규칙을 정의한다.
     */
    private boolean matchesKeyword(BlockchainTransactionItemResponse item, String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return true;
        }
        String normalized = keyword.trim().toLowerCase(Locale.ROOT);
        return contains(item.txHash(), normalized)
                || contains(item.transactionCode(), normalized)
                || contains(item.foundationName(), normalized)
                || contains(item.campaignName(), normalized)
                || contains(item.fromWalletAddress(), normalized)
                || contains(item.toWalletAddress(), normalized);
    }

    /**
     * null-safe 문자열 포함 여부 검사.
     */
    private boolean contains(String value, String keyword) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(keyword);
    }

    /**
     * 상태 파라미터 문자열을 TransactionStatus로 변환한다.
     * 비어 있으면 SUCCESS 기본값, 변환 실패 시 400 에러를 반환한다.
     */
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

    /**
     * 내부 이벤트 타입을 프론트 표준 eventType 코드로 매핑한다.
     */
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

    /**
     * 이벤트 타입을 화면 표시용 한글 라벨로 변환한다.
     */
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

    /**
     * 이벤트 타입별 설명 문구(메모)를 생성한다.
     */
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

    /**
     * 지갑 타입을 API 표준 ownerType 코드로 변환한다.
     */
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

    /**
     * 지갑 타입을 화면 표시용 ownerType 라벨로 변환한다.
     */
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

    /**
     * 거래 발생 시각은 sentAt 우선, 없으면 createdAt을 사용한다.
     */
    private LocalDateTime resolveOccurredAt(Transaction transaction) {
        return transaction.getSentAt() != null ? transaction.getSentAt() : transaction.getCreatedAt();
    }

    /**
     * 메모리 리스트를 페이지 단위로 자르는 공통 유틸.
     * 유효 범위를 벗어난 페이지 요청은 가장 가까운 페이지로 보정한다.
     */
    private <T> PageSlice<T> paginate(List<T> items, int requestedPage, int pageSize) {
        int safePageSize = pageSize <= 0 ? DEFAULT_PAGE_SIZE : pageSize;
        long totalItems = items.size();
        int totalPages = Math.max(1, (int) Math.ceil((double) totalItems / safePageSize));
        int currentPage = Math.max(1, requestedPage);
        if (currentPage > totalPages) {
            currentPage = totalPages;
        }

        int fromIndex = (currentPage - 1) * safePageSize;
        int toIndex = Math.min(fromIndex + safePageSize, items.size());
        List<T> pagedItems = fromIndex >= toIndex ? List.of() : items.subList(fromIndex, toIndex);

        BlockchainPageInfoResponse pageInfo = new BlockchainPageInfoResponse(
                currentPage,
                safePageSize,
                totalItems,
                totalPages
        );
        return new PageSlice<>(pagedItems, pageInfo);
    }

    /**
     * 트랜잭션 화면 표시에 필요한 이름 정보 묶음(내부용).
     */
    private record Names(String foundationName, String campaignName) {
    }

    /**
     * 페이지 계산 결과 묶음(내부용).
     */
    private record PageSlice<T>(List<T> items, BlockchainPageInfoResponse pageInfo) {
    }

    /**
     * 요청 단위 조회 캐시.
     * 같은 PK를 반복 조회할 때 DB round-trip을 줄이기 위해 사용한다.
     */
    private static class LookupCache {
        private final Map<Long, Campaign> campaignByWalletNo = new HashMap<>();
        private final Map<Long, Foundation> foundationByNo = new HashMap<>();
        private final Map<Long, User> userByNo = new HashMap<>();
        private final Map<Long, Beneficiary> beneficiaryByNo = new HashMap<>();
    }
}

package com.merge.final_project.blockchain.repository;

import com.merge.final_project.blockchain.entity.Transaction;
import com.merge.final_project.blockchain.entity.TransactionEventType;
import com.merge.final_project.blockchain.entity.TransactionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    /**
     * 내부 거래코드(UUID)로 거래 목록 조회.
     * 주로 운영/디버깅 시 동일 코드 추적에 사용한다.
     */
    List<Transaction> findByTransactionCode(String transactionCode);

    /**
     * 상태별 거래 목록을 최신순(sentAt, transactionNo DESC)으로 조회.
     * 대시보드 목록/요약 계산의 기본 데이터셋이다.
     */
    List<Transaction> findByStatusOrderBySentAtDescTransactionNoDesc(TransactionStatus status);

    @EntityGraph(attributePaths = {"fromWallet", "toWallet"})
    Page<Transaction> findByStatus(TransactionStatus status, Pageable pageable);

    @EntityGraph(attributePaths = {"fromWallet", "toWallet"})
    @Query(value = """
            SELECT t.*
            FROM token_transaction t
            JOIN wallet fw ON t.from_wallet_no = fw.wallet_no
            JOIN wallet tw ON t.to_wallet_no = tw.wallet_no
            LEFT JOIN campaign cfw ON cfw.wallet_no = fw.wallet_no
            LEFT JOIN campaign ctw ON ctw.wallet_no = tw.wallet_no
            LEFT JOIN foundation ffw
                ON fw.wallet_type = 'FOUNDATION'
               AND ffw.foundation_no = fw.owner_no
            LEFT JOIN foundation ftw
                ON tw.wallet_type = 'FOUNDATION'
               AND ftw.foundation_no = tw.owner_no
            LEFT JOIN foundation fcfw ON fcfw.foundation_no = cfw.foundation_no
            LEFT JOIN foundation fctw ON fctw.foundation_no = ctw.foundation_no
            WHERE t.status = :status
              AND (
                    :keyword = ''
                 OR LOWER(COALESCE(t.tx_hash, '')) LIKE :keyword ESCAPE '\\'
                 OR LOWER(COALESCE(t.transaction_code, '')) LIKE :keyword ESCAPE '\\'
                 OR LOWER(fw.wallet_address) LIKE :keyword ESCAPE '\\'
                 OR LOWER(tw.wallet_address) LIKE :keyword ESCAPE '\\'
                 OR LOWER(COALESCE(cfw.title, ctw.title, '')) LIKE :keyword ESCAPE '\\'
                 OR LOWER(COALESCE(ffw.foundation_name, ftw.foundation_name, fcfw.foundation_name, fctw.foundation_name, '')) LIKE :keyword ESCAPE '\\'
              )
            ORDER BY t.sent_at DESC NULLS LAST, t.transaction_no DESC
            """,
            countQuery = """
                    SELECT COUNT(*)
                    FROM token_transaction t
                    JOIN wallet fw ON t.from_wallet_no = fw.wallet_no
                    JOIN wallet tw ON t.to_wallet_no = tw.wallet_no
                    LEFT JOIN campaign cfw ON cfw.wallet_no = fw.wallet_no
                    LEFT JOIN campaign ctw ON ctw.wallet_no = tw.wallet_no
                    LEFT JOIN foundation ffw
                        ON fw.wallet_type = 'FOUNDATION'
                       AND ffw.foundation_no = fw.owner_no
                    LEFT JOIN foundation ftw
                        ON tw.wallet_type = 'FOUNDATION'
                       AND ftw.foundation_no = tw.owner_no
                    LEFT JOIN foundation fcfw ON fcfw.foundation_no = cfw.foundation_no
                    LEFT JOIN foundation fctw ON fctw.foundation_no = ctw.foundation_no
                    WHERE t.status = :status
                      AND (
                            :keyword = ''
                         OR LOWER(COALESCE(t.tx_hash, '')) LIKE :keyword ESCAPE '\\'
                         OR LOWER(COALESCE(t.transaction_code, '')) LIKE :keyword ESCAPE '\\'
                         OR LOWER(fw.wallet_address) LIKE :keyword ESCAPE '\\'
                         OR LOWER(tw.wallet_address) LIKE :keyword ESCAPE '\\'
                         OR LOWER(COALESCE(cfw.title, ctw.title, '')) LIKE :keyword ESCAPE '\\'
                         OR LOWER(COALESCE(ffw.foundation_name, ftw.foundation_name, fcfw.foundation_name, fctw.foundation_name, '')) LIKE :keyword ESCAPE '\\'
                      )
                    """,
            nativeQuery = true)
    Page<Transaction> searchDashboardPage(
            @Param("status") String status,
            @Param("keyword") String keyword,
            Pageable pageable
    );

    /**
     * 상태별 거래 개수 집계.
     */
    long countByStatus(TransactionStatus status);

    /**
     * txHash(대소문자 무시) 기준 최신 거래 1건 조회.
     * 동일 해시 중복 저장 가능성을 고려해 transactionNo DESC로 최신 건을 선택한다.
     */
    Optional<Transaction> findTopByTxHashIgnoreCaseAndStatusOrderByTransactionNoDesc(
            String txHash,
            TransactionStatus status
    );

    /**
     * 상태 조건에서 blockNum이 있는 거래 중 가장 큰 blockNum 1건 조회.
     * 대시보드 최신 블록 값 계산에 사용한다.
     */
    Optional<Transaction> findTopByStatusAndBlockNumIsNotNullOrderByBlockNumDesc(TransactionStatus status);

    @EntityGraph(attributePaths = {"fromWallet", "toWallet"})
    @Query("""
            SELECT t
            FROM Transaction t
            WHERE t.status = :status
              AND (
                    t.fromWallet.walletNo = :walletNo
                 OR t.toWallet.walletNo = :walletNo
              )
            """)
    Page<Transaction> findPageByWalletNoAndStatus(
            @Param("walletNo") Long walletNo,
            @Param("status") TransactionStatus status,
            Pageable pageable
    );

    /**
     * 특정 지갑 주소가 from/to로 참여한 거래를 상태 조건과 함께 조회.
     * 주소 비교는 LOWER()로 케이스 차이를 무시한다.
     */
    @Query("""
            SELECT t
            FROM Transaction t
            WHERE t.status = :status
              AND (
                    LOWER(t.fromWallet.walletAddress) = LOWER(:walletAddress)
                 OR LOWER(t.toWallet.walletAddress) = LOWER(:walletAddress)
              )
            ORDER BY t.sentAt DESC, t.transactionNo DESC
            """)
    List<Transaction> findByWalletAddressAndStatus(
            @Param("walletAddress") String walletAddress,
            @Param("status") TransactionStatus status
    );

    /**
     * 단일 이벤트 타입의 금액 합계를 집계한다.
     */
    @Query("""
            SELECT COALESCE(SUM(t.amount), 0)
            FROM Transaction t
            WHERE t.status = :status
              AND t.eventType = :eventType
            """)
    Long sumAmountByStatusAndEventType(
            @Param("status") TransactionStatus status,
            @Param("eventType") TransactionEventType eventType
    );

    /**
     * 이벤트 타입 집합(IN)의 금액 합계를 집계한다.
     * 토큰화/정산 등 복수 이벤트를 한 번에 계산할 때 사용한다.
     */
    @Query("""
            SELECT COALESCE(SUM(t.amount), 0)
            FROM Transaction t
            WHERE t.status = :status
              AND t.eventType IN :eventTypes
            """)
    Long sumAmountByStatusAndEventTypes(
            @Param("status") TransactionStatus status,
            @Param("eventTypes") List<TransactionEventType> eventTypes
    );

    //[채원]
    Optional<Transaction>  findByTransactionNo(Long transactionNo);
}

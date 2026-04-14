package com.merge.final_project.blockchain.repository;

import com.merge.final_project.blockchain.entity.Transaction;
import com.merge.final_project.blockchain.entity.TransactionEventType;
import com.merge.final_project.blockchain.entity.TransactionStatus;
import org.springframework.data.domain.Page; // [가빈] 핫월렛 거래내역 페이징용
import org.springframework.data.domain.Pageable; // [가빈]
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

    // [가빈] 특정 지갑 주소가 from/to로 참여한 거래들의 상태와 상관없이, 전체 내역 페이징 조회 (관리자 핫월렛 내역용)
    // 구현된 코드 로직 수정하지 않고 별도 메서드로 구현 -> 관리자 쪽에서 이 메서드만 사용할게요
    @Query("""
            SELECT t
            FROM Transaction t
            WHERE LOWER(t.fromWallet.walletAddress) = LOWER(:walletAddress)
               OR LOWER(t.toWallet.walletAddress) = LOWER(:walletAddress)
            """)
    Page<Transaction> findByWalletAddressPaged(
            @Param("walletAddress") String walletAddress,
            Pageable pageable
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
}

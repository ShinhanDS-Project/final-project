package com.merge.final_project.blockchain.repository;

import com.merge.final_project.blockchain.entity.Transaction;
import com.merge.final_project.blockchain.entity.TransactionEventType;
import com.merge.final_project.blockchain.entity.TransactionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByTransactionCode(String transactionCode);

    List<Transaction> findByStatusOrderBySentAtDescTransactionNoDesc(TransactionStatus status);

    long countByStatus(TransactionStatus status);

    Optional<Transaction> findTopByTxHashIgnoreCaseAndStatusOrderByTransactionNoDesc(
            String txHash,
            TransactionStatus status
    );

    Optional<Transaction> findTopByStatusAndBlockNumIsNotNullOrderByBlockNumDesc(TransactionStatus status);

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

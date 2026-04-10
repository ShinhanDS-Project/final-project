package com.merge.final_project.blockchain.repository;

import com.merge.final_project.blockchain.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    // transactionCode 기준으로 트랜잭션 목록 조회
    List<Transaction> findByTransactionCode(String transactionCode);
}

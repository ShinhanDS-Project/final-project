package com.merge.final_project.blockchain.repository;

import com.merge.final_project.blockchain.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
}

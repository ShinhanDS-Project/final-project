package com.merge.final_project.db.repository;

import com.merge.final_project.db.entity.TokenTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TokenTransactionRepository extends JpaRepository<TokenTransaction, Long> {
}

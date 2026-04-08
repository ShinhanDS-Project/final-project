package com.merge.final_project.db.repository;

import com.merge.final_project.db.entity.Settlement;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SettlementRepository extends JpaRepository<Settlement, Integer> {
}

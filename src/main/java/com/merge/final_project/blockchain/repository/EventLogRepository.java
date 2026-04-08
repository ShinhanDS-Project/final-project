package com.merge.final_project.blockchain.repository;

import com.merge.final_project.blockchain.entity.Eventlog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventLogRepository extends JpaRepository<Eventlog, Long> {
}

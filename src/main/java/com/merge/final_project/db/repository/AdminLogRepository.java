package com.merge.final_project.db.repository;

import com.merge.final_project.db.entity.AdminLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdminLogRepository extends JpaRepository<AdminLog, Integer> {
}


package com.merge.final_project.db.repository;

import com.merge.final_project.db.entity.DailyDashboardStats;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DailyDashboardStatsRepository extends JpaRepository<DailyDashboardStats, Integer> {
}


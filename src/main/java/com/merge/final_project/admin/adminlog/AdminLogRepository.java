package com.merge.final_project.admin.adminlog;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface AdminLogRepository extends JpaRepository<AdminLog, Long> {

    // 관리자 로그 내역 조회. 필터링 기능 포함. 수행타입, 수행대상, 기간 별로 필터링 가능.
    // PostgreSQL이 null 파라미터 타입을 추론 못하는 문제로 native query + CAST 사용.
    // Enum은 String으로 받아서 DB의 varchar 컬럼과 비교.
    @Query(value = """
            SELECT * FROM admin_log
            WHERE (CAST(:actionType AS varchar) IS NULL OR action_type = :actionType)
            AND (CAST(:targetType AS varchar) IS NULL OR target_type = :targetType)
            AND (CAST(:startDate AS timestamp) IS NULL OR created_at >= :startDate)
            AND (CAST(:endDate AS timestamp) IS NULL OR created_at <= :endDate)
            ORDER BY created_at DESC
            """,
           countQuery = """
            SELECT COUNT(*) FROM admin_log
            WHERE (CAST(:actionType AS varchar) IS NULL OR action_type = :actionType)
            AND (CAST(:targetType AS varchar) IS NULL OR target_type = :targetType)
            AND (CAST(:startDate AS timestamp) IS NULL OR created_at >= :startDate)
            AND (CAST(:endDate AS timestamp) IS NULL OR created_at <= :endDate)
            """,
           nativeQuery = true)
    Page<AdminLog> findByFilter(
            @Param("actionType") String actionType,
            @Param("targetType") String targetType,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable
    );
}

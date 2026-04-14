package com.merge.final_project.user.verify;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface EmailVerificationRepository extends JpaRepository<EmailVerification,Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE) // SELECT ... FOR UPDATE 쿼리 발생
    Optional<EmailVerification> findByEmail(String email);
    void deleteByEmail(String email);
    void deleteAllByExpiredAtBefore(LocalDateTime now);

    // @Lock 어노테이션을 통해 비관적 쓰기 잠금을 설정합니다.
    // 실행 시 SQL: SELECT * FROM email_verification WHERE email = ? FOR UPDATE
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT e FROM EmailVerification e WHERE e.email = :email")
    Optional<EmailVerification> findByEmailForUpdate(@Param("email") String email);
}

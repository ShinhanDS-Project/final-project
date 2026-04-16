package com.merge.final_project.user.verify;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface EmailVerificationRepository extends JpaRepository<EmailVerification,Long> {

    Optional<EmailVerification> findByEmail(String email);
    void deleteByEmail(String email);
    void deleteAllByExpiredAtBefore(LocalDateTime now);

    // [수정] 중복 데이터 존재 시 NonUniqueResultException 방지를 위해 List로 반환받습니다.
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT e FROM EmailVerification e WHERE e.email = :email")
    List<EmailVerification> findByEmailForUpdate(@Param("email") String email);
}

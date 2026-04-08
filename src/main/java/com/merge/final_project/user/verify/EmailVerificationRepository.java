package com.merge.final_project.user.verify;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface EmailVerificationRepository extends JpaRepository<EmailVerification,Long> {
    Optional<EmailVerification> findByEmail(String email);
    void deleteByEmail(String email);
    void deleteAllByExpiredAtBefore(LocalDateTime now);
}

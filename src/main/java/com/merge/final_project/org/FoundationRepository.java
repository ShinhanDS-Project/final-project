package com.merge.final_project.org;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FoundationRepository extends JpaRepository<Foundation, Long> {
    boolean existsByBusinessRegistrationNumber(String businessRegistrationNumber);

    Page<Foundation> findByReviewStatus(ReviewStatus reviewStatus, Pageable pageable);

    Page<Foundation> findByReviewStatusNotIn(List<ReviewStatus> reviewStatuses, Pageable pageable);

    Page<Foundation> findByReviewStatusAndAccountStatus(ReviewStatus reviewStatus, AccountStatus accountStatus, Pageable pageable);

    Optional<Foundation> findFirstByFoundationNameIgnoreCase(String foundationName);
}

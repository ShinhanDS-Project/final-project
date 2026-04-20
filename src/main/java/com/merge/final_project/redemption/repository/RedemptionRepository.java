package com.merge.final_project.redemption.repository;

import com.merge.final_project.redemption.RedemptionStatus;
import com.merge.final_project.redemption.RequesterType;
import com.merge.final_project.redemption.entity.Redemption;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RedemptionRepository extends JpaRepository<Redemption, Long> {
    List<Redemption> findAllByStatusOrderByRequestedAtDesc(RedemptionStatus status);

    List<Redemption> findAllByStatusOrderByRedemptionNoAsc(RedemptionStatus status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT r FROM Redemption r WHERE r.redemptionNo = :redemptionNo")
    Optional<Redemption> findByIdForUpdate(@Param("redemptionNo") Long redemptionNo);

    Page<Redemption> findByRequesterTypeAndRequesterNo(RequesterType requesterType, Long requesterNo, Pageable pageable);
}

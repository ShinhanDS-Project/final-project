package com.merge.final_project.redemption.repository;

import com.merge.final_project.redemption.RedemptionStatus;
import com.merge.final_project.redemption.RequesterType;
import com.merge.final_project.redemption.entity.Redemption;
import org.springframework.data.domain.Page; // [가빈] 추가
import org.springframework.data.domain.Pageable; // [가빈] 추가
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RedemptionRepository extends JpaRepository<Redemption, Long> {
    //상태별 조회
    List<Redemption> findAllByStatusOrderByRequestedAtDesc(RedemptionStatus status);

    // [가빈] 기부단체/수혜자 마이페이지 — 본인 환금(현금화) 내역 조회
    Page<Redemption> findByRequesterTypeAndRequesterNo(RequesterType requesterType, Long requesterNo, Pageable pageable);
}

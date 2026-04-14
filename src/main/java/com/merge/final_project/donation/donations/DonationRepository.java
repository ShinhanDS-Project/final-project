package com.merge.final_project.donation.donations;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * 선우 작성:
 * 결제-블록체인 비동기 연계를 위한 donation 상태 전이 전용 repository.
 *
 * 핵심 목표:
 * 1. 상태 전이를 SQL 단에서 조건부(update ... where currentStatus)로 처리해 경합을 줄인다.
 * 2. 재시도 스케줄러가 빠르게 대상 건을 조회할 수 있도록 상태/정렬 조회 메서드를 제공한다.
 */
public interface DonationRepository extends JpaRepository<Donation, Long> {
    boolean existsByPaymentNo(Long paymentNo);

    List<Donation> findByUserNo(Long userNo);

    /**
     * 현재 상태가 expected일 때만 다음 상태로 전이한다.
     * 반환값(영향 row 수)으로 상태 전이 성공 여부를 판단한다.
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            update Donation d
               set d.tokenStatus = :nextStatus
             where d.donationNo = :donationNo
               and d.tokenStatus = :currentStatus
            """)
    int updateTokenStatusIfCurrent(
            @Param("donationNo") Long donationNo,
            @Param("currentStatus") String currentStatus,
            @Param("nextStatus") String nextStatus
    );

    /**
     * 최종 성공 시 상태와 온체인 transactionNo를 함께 반영한다.
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            update Donation d
               set d.tokenStatus = :status,
                   d.transactionNo = :transactionNo
             where d.donationNo = :donationNo
            """)
    int updateStatusAndTransactionNo(
            @Param("donationNo") Long donationNo,
            @Param("status") String status,
            @Param("transactionNo") Long transactionNo
    );

    /**
     * 실패/중간 상태를 단순 전이할 때 사용한다.
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            update Donation d
               set d.tokenStatus = :status
             where d.donationNo = :donationNo
            """)
    int updateStatus(
            @Param("donationNo") Long donationNo,
            @Param("status") String status
    );

    /**
     * 재시도 스케줄러가 한 번에 처리할 후보를 순서대로 조회한다.
     */
    List<Donation> findTop50ByTokenStatusInOrderByDonationNoAsc(List<String> tokenStatus);
}

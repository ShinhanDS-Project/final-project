package com.merge.final_project.donation.payment;

import com.merge.final_project.donation.payment.dto.PaymentByUserResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    //db 저장/조회
    //order 키 기준으로 찾기
    Optional<Payment> findByOrderKey(String orderKey);

    // payment 키 기준으로 찾기
    Optional<Payment> findByPaymentKey(String paymentKey);

    // payment no 기준으로 존재 찾기
    boolean existsByPaymentNo(Long paymentNo);

    boolean existsByPaymentKey(String paymentKey);
    // orderkey와 userno로 payment 찾기
    Optional<Payment> findByOrderKeyAndUserNo(String orderKey, Long userNo);


    List<PaymentByUserResponse> findByUserNo(Long userNo);

    // [가빈] 오늘 완료된 기부액 합계
    @Query(value = "SELECT COALESCE(SUM(p.amount), 0) FROM payment p WHERE p.payment_status = 'COMPLETED' AND DATE(p.paid_at) = CURRENT_DATE", nativeQuery = true)
    BigDecimal sumTodayCompletedAmount();

    // [가빈] 누적 완료된 기부액 합계
    @Query(value = "SELECT COALESCE(SUM(p.amount), 0) FROM payment p WHERE p.payment_status = 'COMPLETED'", nativeQuery = true)
    BigDecimal sumTotalCompletedAmount();

    // [가빈] 일별 기부액 추이 (since 이후, COMPLETED만)
    // Object[] = [date(String), amount(BigDecimal)]
    @Query(value = """
        SELECT TO_CHAR(DATE(p.paid_at), 'YYYY-MM-DD') AS trend_date,
               COALESCE(SUM(p.amount), 0) AS total_amount
        FROM payment p
        WHERE p.payment_status = 'COMPLETED'
          AND p.paid_at >= :since
        GROUP BY DATE(p.paid_at)
        ORDER BY DATE(p.paid_at)
        """, nativeQuery = true)
    List<Object[]> findDailyDonationTrend(@Param("since") LocalDateTime since);

    // [가빈] 카테고리별 기부금 합계
    // Object[] = [category(String), amount(BigDecimal)]
    @Query(value = """
        SELECT c.category, COALESCE(SUM(p.amount), 0) AS total_amount
        FROM payment p
        JOIN campaign c ON p.campaign_no = c.campaign_no
        WHERE p.payment_status = 'COMPLETED'
        GROUP BY c.category
        """, nativeQuery = true)
    List<Object[]> findDonationAmountByCategory();
}

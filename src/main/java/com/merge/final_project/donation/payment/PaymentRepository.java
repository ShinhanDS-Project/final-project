package com.merge.final_project.donation.payment;

import com.merge.final_project.donation.payment.dto.PaymentByUserResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 결제 도메인 조회/집계 repository.
 *
 * 선우 작성 메모:
 * 아래 메서드 중 결제-블록체인 연계에서 직접 참조되는 핵심 메서드는
 * findByOrderKeyAndUserNo / existsByPaymentKey / findByUserNo 이다.
 * 나머지 통계 쿼리는 관리자 대시보드 집계 용도로 유지한다.
 */
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    /**
     * 주문번호 단건 조회.
     */
    Optional<Payment> findByOrderKey(String orderKey);

    /**
     * 외부 결제사 결제키 단건 조회.
     */
    Optional<Payment> findByPaymentKey(String paymentKey);

    /**
     * 내부 paymentNo 존재 여부 확인.
     */
    boolean existsByPaymentNo(Long paymentNo);

    /**
     * 외부 paymentKey 중복 방지 체크.
     */
    boolean existsByPaymentKey(String paymentKey);

    /**
     * 사용자 본인 결제건 검증용 조회.
     */
    Optional<Payment> findByOrderKeyAndUserNo(String orderKey, Long userNo);

    /**
     * 마이페이지 결제 이력 조회.
     */
    List<PaymentByUserResponse> findByUserNo(Long userNo);

    // [가빈] 오늘 완료된 기부액 합계
    @Query(value = "SELECT COALESCE(SUM(p.amount), 0) FROM payment p WHERE p.payment_status = 'DONE' AND DATE(p.paid_at) = CURRENT_DATE", nativeQuery = true)
    BigDecimal sumTodayCompletedAmount();

    // [가빈] 누적 완료된 기부액 합계
    @Query(value = "SELECT COALESCE(SUM(p.amount), 0) FROM payment p WHERE p.payment_status = 'DONE'", nativeQuery = true)
    BigDecimal sumTotalCompletedAmount();

    // [가빈] 일별 기부액 추이 (since 이후, DONE만)
    // Object[] = [date(String), amount(BigDecimal)]
    @Query(value = """
        SELECT TO_CHAR(DATE(p.paid_at), 'YYYY-MM-DD') AS trend_date,
               COALESCE(SUM(p.amount), 0) AS total_amount
        FROM payment p
        WHERE p.payment_status = 'DONE'
          AND p.paid_at >= :since
        GROUP BY DATE(p.paid_at)
        ORDER BY DATE(p.paid_at)
        """, nativeQuery = true)
    List<Object[]> findDailyDonationTrend(@Param("since") LocalDateTime since);

    // [가빈] 기부단체별 이번달 모금액
    @Query(value = """
        SELECT COALESCE(SUM(p.amount), 0)
        FROM payment p
        JOIN campaign c ON p.campaign_no = c.campaign_no
        WHERE p.payment_status = 'DONE'
          AND c.foundation_no = :foundationNo
          AND DATE_TRUNC('month', p.paid_at) = DATE_TRUNC('month', CURRENT_DATE)
        """, nativeQuery = true)
    BigDecimal sumThisMonthAmountByFoundationNo(@Param("foundationNo") Long foundationNo);

    // [가빈] 카테고리별 기부금 합계
    // Object[] = [category(String), amount(BigDecimal)]
    @Query(value = """
        SELECT c.category, COALESCE(SUM(p.amount), 0) AS total_amount
        FROM payment p
        JOIN campaign c ON p.campaign_no = c.campaign_no
        WHERE p.payment_status = 'DONE'
        GROUP BY c.category
        """, nativeQuery = true)
    List<Object[]> findDonationAmountByCategory();
}

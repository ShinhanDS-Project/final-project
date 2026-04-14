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

    /**
     * 관리자 대시보드: 오늘 완료 결제 합계.
     */
    @Query(value = "SELECT COALESCE(SUM(p.amount), 0) FROM payment p WHERE p.payment_status = 'COMPLETED' AND DATE(p.paid_at) = CURRENT_DATE", nativeQuery = true)
    BigDecimal sumTodayCompletedAmount();

    /**
     * 관리자 대시보드: 전체 완료 결제 합계.
     */
    @Query(value = "SELECT COALESCE(SUM(p.amount), 0) FROM payment p WHERE p.payment_status = 'COMPLETED'", nativeQuery = true)
    BigDecimal sumTotalCompletedAmount();

    /**
     * 관리자 대시보드: 일별 결제 추이.
     * 반환 형식 Object[] = [date(String), amount(BigDecimal)]
     */
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

    /**
     * 관리자 대시보드: 카테고리별 결제 합계.
     * 반환 형식 Object[] = [category(String), amount(BigDecimal)]
     */
    @Query(value = """
        SELECT c.category, COALESCE(SUM(p.amount), 0) AS total_amount
        FROM payment p
        JOIN campaign c ON p.campaign_no = c.campaign_no
        WHERE p.payment_status = 'COMPLETED'
        GROUP BY c.category
        """, nativeQuery = true)
    List<Object[]> findDonationAmountByCategory();
}

package com.merge.final_project.donation.payment;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    //db 저장/조회
    //order 키 기준으로 찾기
    Optional<Payment> findByOrderKey(String orderKey);

    // payment 키 기준으로 찾기
    Optional<Payment> findByPaymentKey(String paymentKey);

    // payment no 기준으로 존재 찾기
    boolean existsByPaymentNo(Long paymentNo);
}

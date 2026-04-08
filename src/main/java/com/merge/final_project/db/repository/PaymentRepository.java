package com.merge.final_project.db.repository;

import com.merge.final_project.db.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, Integer> {
}


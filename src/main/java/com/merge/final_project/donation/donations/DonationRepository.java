package com.merge.final_project.donation.donations;

import org.springframework.data.jpa.repository.JpaRepository;

public interface DonationRepository extends JpaRepository<Donation, Long> {
    boolean existsByPaymentNo(Long paymentNo);
}

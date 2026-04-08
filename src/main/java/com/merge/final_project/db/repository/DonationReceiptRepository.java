package com.merge.final_project.db.repository;

import com.merge.final_project.db.entity.DonationReceipt;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DonationReceiptRepository extends JpaRepository<DonationReceipt, String> {
}


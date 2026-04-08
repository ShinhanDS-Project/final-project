package com.merge.final_project.db.repository;

import com.merge.final_project.db.entity.Donation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DonationRepository extends JpaRepository<Donation, String> {
}

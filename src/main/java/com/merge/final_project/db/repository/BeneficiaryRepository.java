package com.merge.final_project.db.repository;

import com.merge.final_project.db.entity.Beneficiary;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BeneficiaryRepository extends JpaRepository<Beneficiary, Integer> {
}


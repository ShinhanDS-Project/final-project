package com.merge.final_project.redemption.repository;

import com.merge.final_project.redemption.entity.Redemption;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RedemptionRepository extends JpaRepository<Redemption, Long> {
    List<Redemption> findAllByOrderByRequestedAtDesc();
}

package com.merge.final_project.db.repository;

import com.merge.final_project.db.entity.Campaign;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CampaignRepository extends JpaRepository<Campaign, Integer> {
}


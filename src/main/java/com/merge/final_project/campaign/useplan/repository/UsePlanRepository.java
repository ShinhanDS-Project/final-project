package com.merge.final_project.campaign.useplan.repository;

import com.merge.final_project.campaign.useplan.entity.UsePlan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UsePlanRepository extends JpaRepository<UsePlan, Long> {
    List<UsePlan> findByCampaignNoOrderByUsePlanNoAsc(Long campaignNo);

    void deleteByCampaignNo(Long campaignNo);
}

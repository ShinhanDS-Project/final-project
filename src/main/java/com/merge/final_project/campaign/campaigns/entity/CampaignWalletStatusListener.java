package com.merge.final_project.campaign.campaigns.entity;

import com.merge.final_project.campaign.campaigns.ApprovalStatus;
import com.merge.final_project.campaign.campaigns.CampaignStatus;
import jakarta.persistence.PostUpdate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class CampaignWalletStatusListener {
    private static JdbcTemplate jdbcTemplate;

    public CampaignWalletStatusListener() {
    }

    @Autowired
    public CampaignWalletStatusListener(JdbcTemplate jdbcTemplate) {
        CampaignWalletStatusListener.jdbcTemplate = jdbcTemplate;
    }

    @PostUpdate
    public void deactivateWalletWhenCampaignClosed(Campaign campaign) {
        if (jdbcTemplate == null || campaign.getWalletNo() == null) {
            return;
        }

        boolean shouldDeactivate =
                ApprovalStatus.REJECTED.equals(campaign.getApprovalStatus())
                        || CampaignStatus.COMPLETED.equals(campaign.getCampaignStatus());

        if (shouldDeactivate) {
            jdbcTemplate.update(
                    "UPDATE wallet SET status = 'INACTIVE' WHERE wallet_no = ? AND status = 'ACTIVE'",
                    campaign.getWalletNo()
            );
        }
    }
}

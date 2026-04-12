package com.merge.final_project.campaign.campaigns.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class CampaignFoundationCheckResponseDTO {
    private Long foundationNo;
    private String foundationName;
    private boolean hasAvailableWallet;
    private String message;
    private List<WalletStatusItem> wallets;

    @Getter
    @Builder
    public static class WalletStatusItem {
        private String walletLabel;
        private String walletAddress;
        private String status;
        private boolean available;
    }
}

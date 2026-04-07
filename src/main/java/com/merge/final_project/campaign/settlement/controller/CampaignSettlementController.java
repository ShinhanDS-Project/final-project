package com.merge.final_project.campaign.settlement.controller;

import com.merge.final_project.campaign.settlement.service.CampaignSettlementService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/test/settlement")
public class CampaignSettlementController {
    private final CampaignSettlementService campaignSettlementService;

    @PostMapping("/run")
    public ResponseEntity<String> runSettlement() {
        campaignSettlementService.settleAll();
        return ResponseEntity.ok("정산배치 수동실행 완료");
    }
}

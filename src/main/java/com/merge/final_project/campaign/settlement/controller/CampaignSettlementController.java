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

//    //[이채원] 마이페이지에서 -> CAMPAIGNNO 기준으로 정산한 내용 조회 API
//    @GetMapping("/see")
//    public ResponseEntity<SelectSettlementResponseDTO> requestSelectRedemption(Long campaignNo){
//        //service 단에서 조회해오기
//        SelectSettlementResponseDTO response = campaignSettlementService.showSettle(campaignNo);
//        return ResponseEntity.ok(response);
//    }
}

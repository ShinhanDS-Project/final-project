package com.merge.final_project.campaign.settlement.controller;

import com.merge.final_project.campaign.settlement.service.CampaignSettlementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@io.swagger.v3.oas.annotations.tags.Tag(name = "정산 테스트", description = "정산 배치 수동 실행 API (개발·테스트용)")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/test/settlement")
public class CampaignSettlementController {
    private final CampaignSettlementService campaignSettlementService;

    @Operation(summary = "정산 배치 수동 실행", description = "ENDED 상태 캠페인에 대해 정산 배치를 수동으로 실행합니다. 개발·테스트 전용입니다.")
    @ApiResponses({ @ApiResponse(responseCode = "200", description = "정산 완료") })
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

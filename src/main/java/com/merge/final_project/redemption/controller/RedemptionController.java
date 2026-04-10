package com.merge.final_project.redemption.controller;

import com.merge.final_project.redemption.dto.request.RedemptionRequest;
import com.merge.final_project.redemption.dto.response.RedemptionResponse;
import com.merge.final_project.redemption.service.RedemptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/redemptions")
public class RedemptionController {

    private final RedemptionService redemptionService;

    // 현금화 요청 API
    // 요청자(기부단체/수혜자)가 본인 지갑의 토큰을 현금화 요청
    @PostMapping
    public ResponseEntity<RedemptionResponse> requestRedemption(
            @RequestBody RedemptionRequest request
    ) {
        // 서비스에서 현금화 로직 수행 후 결과 반환
        return ResponseEntity.ok(redemptionService.requestRedemption(request));
    }
}

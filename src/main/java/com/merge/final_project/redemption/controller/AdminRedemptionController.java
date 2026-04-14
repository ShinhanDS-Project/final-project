package com.merge.final_project.redemption.controller;

import com.merge.final_project.redemption.RedemptionStatus;
import com.merge.final_project.redemption.dto.response.RedemptionDetailResponse;
import com.merge.final_project.redemption.dto.response.RedemptionListResponse;
import com.merge.final_project.redemption.service.RedemptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@io.swagger.v3.oas.annotations.tags.Tag(name = "관리자 환금 관리", description = "관리자 토큰 환금(현금화) 처리 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/redemptions")
public class AdminRedemptionController {

    private final RedemptionService redemptionService;

    // 관리자용 현금화 요청 목록 조회 (상태 필터 선택 가능)
    @GetMapping
    public ResponseEntity<List<RedemptionListResponse>> getRedemptions(
            @RequestParam(required = false) RedemptionStatus status
    ) {
        return ResponseEntity.ok(redemptionService.getAdminRedemptions(status));
    }
    // 관리자용 현금화 요청 상세 조회
    @GetMapping("/{redemptionNo}")
    public ResponseEntity<RedemptionDetailResponse> getRedemptionDetail(@PathVariable Long redemptionNo) {
        return ResponseEntity.ok(redemptionService.getAdminRedemptionDetail(redemptionNo));
    }
    // 현금 지급 완료 처리 (COMPLETED → PAID)
    @PatchMapping("/{redemptionNo}/pay")
    public ResponseEntity<Void> markCashPaid(@PathVariable Long redemptionNo) {
        redemptionService.markCashPaid(redemptionNo);
        return ResponseEntity.ok().build();
    }
}

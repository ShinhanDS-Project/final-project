package com.merge.final_project.redemption.controller;

import com.merge.final_project.redemption.RequesterType;
import com.merge.final_project.redemption.dto.request.RedemptionRequest;
import com.merge.final_project.redemption.dto.response.RedemptionResponse;
import com.merge.final_project.redemption.service.RedemptionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.*;

@io.swagger.v3.oas.annotations.tags.Tag(name = "환금(현금화)", description = "기부단체 토큰 환금(현금화) 신청 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/redemptions")
public class RedemptionController {

    private final RedemptionService redemptionService;

    @Operation(summary = "토큰 환금 신청", description = "기부단체 또는 수혜자가 보유 토큰의 현금 환금을 신청합니다. JWT 토큰에서 요청자 정보를 자동 추출합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "신청 성공"),
            @ApiResponse(responseCode = "400", description = "요청 값 유효성 오류"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 요청")
    })
    @PostMapping
    public ResponseEntity<RedemptionResponse> requestRedemption(
            Authentication authentication,
            @RequestBody RedemptionRequest request
    ) {
        // 요청 body의 requester 정보는 무시하고, 인증된 사용자 정보 사용
        Long requesterNo = (Long) authentication.getDetails();
        // 로그인 사용자의 권한(role) 추출
        String role = authentication.getAuthorities().stream()
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("authority not found"))
                .getAuthority();

        // role에 따라 요청자 타입 매핑
        RequesterType requesterType = switch (role) {
            case "ROLE_FOUNDATION" -> RequesterType.FOUNDATION;
            case "ROLE_BENEFICIARY" -> RequesterType.BENEFICIARY;
            default -> throw new IllegalArgumentException("unsupported redemption role: " + role);
        };

        // 환전 요청 처리 후 결과 반환
        return ResponseEntity.ok(redemptionService.requestRedemption(request, requesterType, requesterNo));
    }
}

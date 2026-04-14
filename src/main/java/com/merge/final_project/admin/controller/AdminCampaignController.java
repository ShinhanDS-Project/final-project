package com.merge.final_project.admin.controller;

import com.merge.final_project.admin.service.AdminCampaignService;
import com.merge.final_project.campaign.campaigns.dto.CampaignListResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "관리자 캠페인 관리", description = "관리자 캠페인 승인/반려 API")
@RestController
@RequestMapping("/admin/campaigns")
@RequiredArgsConstructor
public class AdminCampaignController {

    private final AdminCampaignService adminCampaignService;

    @Operation(summary = "승인 대기 캠페인 목록 조회", description = "승인 대기(PENDING) 상태의 캠페인 목록을 키워드(제목) 검색과 페이징으로 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 요청"),
            @ApiResponse(responseCode = "403", description = "관리자 권한 없음")
    })
    @GetMapping("/pending")
    public ResponseEntity<Page<CampaignListResponseDTO>> getPendingCampaigns(
            @Parameter(description = "캠페인 제목 키워드 검색", example = "어린이")
            @RequestParam(defaultValue = "") String keyword,
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(adminCampaignService.getPendingCampaigns(keyword, pageable));
    }

    @Operation(summary = "반려된 캠페인 목록 조회", description = "반려(REJECTED) 상태의 캠페인 목록을 키워드(제목) 검색과 페이징으로 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 요청"),
            @ApiResponse(responseCode = "403", description = "관리자 권한 없음")
    })
    @GetMapping("/rejected")
    public ResponseEntity<Page<CampaignListResponseDTO>> getRejectedCampaigns(
            @Parameter(description = "캠페인 제목 키워드 검색", example = "어린이")
            @RequestParam(defaultValue = "") String keyword,
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(adminCampaignService.getRejectedCampaigns(keyword, pageable));
    }

    @Operation(summary = "승인된 캠페인 목록 조회", description = "승인(APPROVED) 상태의 캠페인 목록을 키워드(제목) 검색과 페이징으로 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 요청"),
            @ApiResponse(responseCode = "403", description = "관리자 권한 없음")
    })
    @GetMapping("/approved")
    public ResponseEntity<Page<CampaignListResponseDTO>> getApprovedCampaigns(
            @Parameter(description = "캠페인 제목 키워드 검색", example = "어린이")
            @RequestParam(defaultValue = "") String keyword,
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(adminCampaignService.getApprovedCampaigns(keyword, pageable));
    }

    @Operation(summary = "캠페인 승인", description = "지정한 캠페인을 승인합니다. 승인 시 캠페인 상태가 RECRUITING으로 전환되고 기부단체에 인앱 알림이 발송됩니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "승인 성공"),
            @ApiResponse(responseCode = "404", description = "캠페인을 찾을 수 없음"),
            @ApiResponse(responseCode = "409", description = "이미 처리된 캠페인"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 요청"),
            @ApiResponse(responseCode = "403", description = "관리자 권한 없음")
    })
    @PatchMapping("/{campaignNo}/approve")
    public ResponseEntity<Void> approve(
            @Parameter(description = "캠페인 번호", example = "1") @PathVariable Long campaignNo) {
        adminCampaignService.approveCampaign(campaignNo);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "캠페인 반려", description = "지정한 캠페인을 반려합니다. 반려 사유가 저장되고 기부단체에 인앱 알림이 발송됩니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "반려 성공"),
            @ApiResponse(responseCode = "404", description = "캠페인을 찾을 수 없음"),
            @ApiResponse(responseCode = "409", description = "이미 처리된 캠페인"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 요청"),
            @ApiResponse(responseCode = "403", description = "관리자 권한 없음")
    })
    @PatchMapping("/{campaignNo}/reject")
    public ResponseEntity<Void> reject(
            @Parameter(description = "캠페인 번호", example = "1") @PathVariable Long campaignNo,
            @Parameter(description = "반려 사유", required = true, example = "캠페인 내용이 부적절합니다.") @RequestParam String reason) {
        adminCampaignService.rejectCampaign(campaignNo, reason);
        return ResponseEntity.ok().build();
    }
}
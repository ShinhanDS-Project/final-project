package com.merge.final_project.admin.controller;

import com.merge.final_project.admin.service.AdminCampaignService;
import com.merge.final_project.campaign.campaigns.dto.CampaignListResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/campaigns")
@RequiredArgsConstructor
public class AdminCampaignController {

    private final AdminCampaignService adminCampaignService;

    // 승인 대기 캠페인 목록 — 키워드(제목) 검색 + 페이징 (기본: 최신순)
    @GetMapping("/pending")
    public ResponseEntity<Page<CampaignListResponseDTO>> getPendingCampaigns(
            @RequestParam(required = false) String keyword,
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(adminCampaignService.getPendingCampaigns(keyword, pageable));
    }

    // 반려된 캠페인 목록 — 키워드(제목) 검색 + 페이징 (기본: 최신순)
    @GetMapping("/rejected")
    public ResponseEntity<Page<CampaignListResponseDTO>> getRejectedCampaigns(
            @RequestParam(required = false) String keyword,
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(adminCampaignService.getRejectedCampaigns(keyword, pageable));
    }

    // 승인된 캠페인 목록 — 키워드(제목) 검색 + 페이징 (기본: 최신순)
    @GetMapping("/approved")
    public ResponseEntity<Page<CampaignListResponseDTO>> getApprovedCampaigns(
            @RequestParam(required = false) String keyword,
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(adminCampaignService.getApprovedCampaigns(keyword, pageable));
    }

    // 캠페인 승인
    @PatchMapping("/{campaignNo}/approve")
    public ResponseEntity<Void> approve(@PathVariable Long campaignNo) {
        adminCampaignService.approveCampaign(campaignNo);
        return ResponseEntity.ok().build();
    }

    // 캠페인 반려
    @PatchMapping("/{campaignNo}/reject")
    public ResponseEntity<Void> reject(@PathVariable Long campaignNo,
                                       @RequestParam String reason) {
        adminCampaignService.rejectCampaign(campaignNo, reason);
        return ResponseEntity.ok().build();
    }
}
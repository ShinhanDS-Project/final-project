package com.merge.final_project.admin.controller;

import com.merge.final_project.admin.service.AdminCampaignService;
import com.merge.final_project.campaign.campaigns.dto.CampaignListResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/campaigns")
@RequiredArgsConstructor
public class AdminCampaignController {

    private final AdminCampaignService adminCampaignService;

    // 승인 대기 캠페인 목록
    @GetMapping("/pending")
    public ResponseEntity<Page<CampaignListResponseDto>> getPendingCampaigns(Pageable pageable) {
        return ResponseEntity.ok(adminCampaignService.getPendingCampaigns(pageable));
    }

    // 반려된 캠페인 목록
    @GetMapping("/rejected")
    public ResponseEntity<Page<CampaignListResponseDto>> getRejectedCampaigns(Pageable pageable) {
        return ResponseEntity.ok(adminCampaignService.getRejectedCampaigns(pageable));
    }

    // 승인된 캠페인 목록 => 승인 됐으니까 진행 중인 애들을 찾아옴.
    @GetMapping("/approved")
    public ResponseEntity<Page<CampaignListResponseDto>> getApprovedCampaigns(Pageable pageable) {
        return ResponseEntity.ok(adminCampaignService.getApprovedCampaigns(pageable));
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

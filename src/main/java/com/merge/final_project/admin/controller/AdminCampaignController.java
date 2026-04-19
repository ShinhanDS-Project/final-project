package com.merge.final_project.admin.controller;

import com.merge.final_project.admin.dto.AdminCampaignDTO;
import com.merge.final_project.admin.service.AdminCampaignService;
import com.merge.final_project.campaign.campaigns.dto.CampaignListResponseDTO;
import com.merge.final_project.campaign.campaigns.entity.Campaign;
import com.merge.final_project.global.Image;
import com.merge.final_project.global.ImageRepository;
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
    private final ImageRepository imageRepository;

    // 승인 대기 캠페인 목록 — 키워드(제목) 검색 + 페이징 (기본: 최신순)
    @GetMapping("/pending")
    public ResponseEntity<Page<CampaignListResponseDTO>> getPendingCampaigns(
            @RequestParam(defaultValue = "") String keyword,
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(adminCampaignService.getPendingCampaigns(keyword, pageable));
    }

    // 반려된 캠페인 목록 — 키워드(제목) 검색 + 페이징 (기본: 최신순)
    @GetMapping("/rejected")
    public ResponseEntity<Page<CampaignListResponseDTO>> getRejectedCampaigns(
            @RequestParam(defaultValue = "") String keyword,
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(adminCampaignService.getRejectedCampaigns(keyword, pageable));
    }

    // 승인된 캠페인 목록 — 키워드(제목) 검색 + 페이징 (기본: 최신순)
    @GetMapping("/approved")
    public ResponseEntity<Page<CampaignListResponseDTO>> getApprovedCampaigns(
            @RequestParam(defaultValue = "") String keyword,
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

    // [가빈] 관리자용 캠페인 상세보기 — Image 테이블에서 이미지 경로 우선 조회
    @GetMapping("/{campaignNo}/detail")
    public ResponseEntity<AdminCampaignDTO> getCampaignDetail(@PathVariable Long campaignNo) {
        Campaign campaign = adminCampaignService.getCampaignDetail(campaignNo);
        String imagePath = imageRepository.findByTargetNameAndTargetNo("campaign", campaignNo)
                .stream()
                .map(Image::getImgPath)
                .findFirst()
                .orElse(campaign.getImagePath());
        return ResponseEntity.ok(AdminCampaignDTO.from(campaign, imagePath));
    }

}
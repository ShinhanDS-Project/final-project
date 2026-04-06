package com.merge.final_project.campaign.campaigns.controller;

import com.merge.final_project.campaign.campaigns.dto.CampaignRequestDto;
import com.merge.final_project.campaign.campaigns.service.CampaignService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/campaigns/register")
@Log4j2
@RequiredArgsConstructor
public class CampaignController {
    private final CampaignService campaignService;

    // 캠페인 등록 (신청 완료 버튼 클릭 시 호출)
    @PostMapping(value = "/form", consumes = {"multipart/form-data"})
    public ResponseEntity<String> register(
            @RequestPart("dto") CampaignRequestDto dto,
            @RequestPart(value = "imageFile", required = false) MultipartFile imageFile
    ) {
        // 이후 인증 정보 가져오도록 수정 필요
        Long foundationNo = 1L;

        campaignService.registerCampaign(dto, imageFile, foundationNo);

        log.info("캠페인 등록 신청 성공");
        return ResponseEntity.ok("캠페인 등록 신청 완료");
    }
}
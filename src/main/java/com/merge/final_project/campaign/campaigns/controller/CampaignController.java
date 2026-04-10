package com.merge.final_project.campaign.campaigns.controller;

import com.merge.final_project.campaign.campaigns.dto.CampaignListResponseDTO;
import com.merge.final_project.campaign.campaigns.dto.CampaignRequestDTO;
import com.merge.final_project.campaign.campaigns.service.CampaignService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/campaigns")
@Log4j2
@RequiredArgsConstructor
public class CampaignController {
    private final CampaignService campaignService;

    // 캠페인 목록 조회
    @GetMapping
    public ResponseEntity<List<CampaignListResponseDTO>> getCampaignList(
            // 정렬 기준 (기본값: 마감임박순)
            @RequestParam(value = "sort", defaultValue = "deadline") String sort
    ) {
        // 백엔드 정렬을 위해 파라미터를 서비스 계층으로 전달
        return ResponseEntity.ok(campaignService.getCampaignList(sort));
    }

    // 신규 캠페인 등록
    @PostMapping(value = "/register/form", consumes = {"multipart/form-data"})
    public ResponseEntity<String> register(
            @RequestPart("dto") CampaignRequestDTO dto,
            @RequestPart(value = "imageFile", required = false) MultipartFile imageFile,
            @RequestPart(value = "detailImageFiles", required = false) List<MultipartFile> detailImageFiles
    ) {
        // 임시 기관 단체 번호 (추후 시큐리티 도입 시 세션에서 추출)
        Long foundationNo = 1L;

        campaignService.registerCampaign(dto, imageFile, detailImageFiles, foundationNo);

        return ResponseEntity.ok("캠페인 등록 신청 완료");
    }
}


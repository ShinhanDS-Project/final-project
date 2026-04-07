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

    // 캠페??목록 조회
    @GetMapping
    public ResponseEntity<List<CampaignListResponseDTO>> getCampaignList(
            // ?�렬 기�? (기본�? 마감?�박??
            @RequestParam(value = "sort", defaultValue = "deadline") String sort
    ) {
        // 백엔???�렬 ?�해 ?�라미터�??�비??계층?�로 ?��?
        return ResponseEntity.ok(campaignService.getCampaignList(sort));
    }

    // ?�규 캠페???�록
    @PostMapping(value = "/register/form", consumes = {"multipart/form-data"})
    public ResponseEntity<String> register(
            @RequestPart("dto") CampaignRequestDTO dto,
            @RequestPart(value = "imageFile", required = false) MultipartFile imageFile,
            @RequestPart(value = "detailImageFiles", required = false) List<MultipartFile> detailImageFiles
    ) {
        // ?�시 기�??�체 번호 (추후 ?�큐리티 ?�입 ???�션?�서 추출)
        Long foundationNo = 1L;

        campaignService.registerCampaign(dto, imageFile, detailImageFiles, foundationNo);

        return ResponseEntity.ok("캠페???�록 ?�청 ?�료");
    }
}


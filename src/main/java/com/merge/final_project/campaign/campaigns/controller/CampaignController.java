package com.merge.final_project.campaign.campaigns.controller;

import com.merge.final_project.campaign.campaigns.dto.CampaignListResponseDTO;
import com.merge.final_project.campaign.campaigns.dto.CampaignRequestDTO;
import com.merge.final_project.campaign.campaigns.service.CampaignService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Controller
@RequestMapping
@Log4j2
@RequiredArgsConstructor
public class CampaignController {
    private final CampaignService campaignService;

    @GetMapping("/campaign/list")
    public String campaignListPage() {
        return "campaign/list";
    }

    @GetMapping("/campaign/register")
    public String campaignRegisterPage() {
        return "campaign/register";
    }

    @GetMapping("/campaigns")
    @ResponseBody
    public ResponseEntity<List<CampaignListResponseDTO>> getCampaignList(
            @RequestParam(value = "sort", defaultValue = "deadline") String sort
    ) {
        return ResponseEntity.ok(campaignService.getCampaignList(sort));
    }

    @PostMapping(value = "/campaigns/register/form", consumes = {"multipart/form-data"})
    @ResponseBody
    public ResponseEntity<String> register(
            @RequestPart("dto") CampaignRequestDTO dto,
            @RequestPart(value = "imageFile", required = false) MultipartFile imageFile,
            @RequestPart(value = "detailImageFiles", required = false) List<MultipartFile> detailImageFiles
    ) {
        Long foundationNo = 1L;

        campaignService.registerCampaign(dto, imageFile, detailImageFiles, foundationNo);

        return ResponseEntity.ok("캠페인 등록 요청 완료");
    }
}

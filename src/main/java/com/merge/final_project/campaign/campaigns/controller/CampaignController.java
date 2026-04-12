package com.merge.final_project.campaign.campaigns.controller;

import com.merge.final_project.campaign.campaigns.dto.CampaignBeneficiaryCheckResponseDTO;
import com.merge.final_project.campaign.campaigns.dto.CampaignDetailResponseDTO;
import com.merge.final_project.campaign.campaigns.dto.CampaignFoundationCheckResponseDTO;
import com.merge.final_project.campaign.campaigns.dto.CampaignListResponseDTO;
import com.merge.final_project.campaign.campaigns.dto.CampaignRequestDTO;
import com.merge.final_project.campaign.campaigns.service.CampaignService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Controller
@RequestMapping("/api/foundation/campaigns")
@Log4j2
@RequiredArgsConstructor
public class CampaignController {
    private final CampaignService campaignService;

    @ResponseBody
    @GetMapping
    public ResponseEntity<List<CampaignListResponseDTO>> getCampaignList(
        @RequestParam(value = "sort", defaultValue = "deadline") String sort
    ) {
        return ResponseEntity.ok(campaignService.getCampaignList(sort));
    }

    @ResponseBody
    @PostMapping(value = "/register", consumes = {"multipart/form-data"})
    public ResponseEntity<String> register(
        @RequestPart("dto") CampaignRequestDTO dto,
        @RequestPart(value = "imageFile", required = false) MultipartFile imageFile,
        @RequestPart(value = "detailImageFiles", required = false) List<MultipartFile> detailImageFiles,
        @RequestParam("foundationNo") Long foundationNo
    ) {
        campaignService.registerCampaign(dto, imageFile, detailImageFiles, foundationNo);
        return ResponseEntity.ok("캠페인 등록 요청 완료");
    }

    @ResponseBody
    @GetMapping("/beneficiary-check")
    public ResponseEntity<CampaignBeneficiaryCheckResponseDTO> checkBeneficiary(@RequestParam String entryCode) {
        return ResponseEntity.ok(campaignService.checkBeneficiaryByEntryCode(entryCode));
    }

    @ResponseBody
    @GetMapping("/foundation-check")
    public ResponseEntity<CampaignFoundationCheckResponseDTO> checkFoundation(@RequestParam Long foundationNo) {
        return ResponseEntity.ok(campaignService.checkFoundationWalletStatus(foundationNo));
    }

    @GetMapping("/register")
    public String getRegisterPage() {
        return "campaign/register";
    }

    @GetMapping("/list")
    public String getCampaignListPage() {
        return "campaign/list";
    }

    @GetMapping("/{campaignNo}")
    public String getCampaignDetailPage(@PathVariable Long campaignNo, Model model) {
        CampaignDetailResponseDTO campaign = campaignService.getCampaignDetail(campaignNo);
        model.addAttribute("campaign", campaign);
        return "campaign/detail";
    }
}

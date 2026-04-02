package com.merge.final_project.campaign.campaigns.controller;

import com.merge.final_project.campaign.campaigns.dto.BeneficiaryResponseDto;
import com.merge.final_project.campaign.campaigns.dto.CampaignRequestDto;
import com.merge.final_project.campaign.campaigns.service.CampaignService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/campaigns/register")
@Log4j2
@RequiredArgsConstructor
public class CampaignController {
    private final CampaignService campaignService;

    // 수혜자 참여코드 확인(확인 버튼 클릭 시 호출)
    @GetMapping("/entrycode")
    public ResponseEntity<BeneficiaryResponseDto> verify(@RequestParam("entryCode") Integer entryCode) {
        log.info("수혜자 참여코드: {}", entryCode);
        BeneficiaryResponseDto response = campaignService.verifyBeneficiary(entryCode);

        log.info("수혜자 확인 완료: {}", response.getName());
        return ResponseEntity.ok(response);
    }

    // 캠페인 등록(신청 완료 버튼 클릭 시 호출)
    @PostMapping("/form")
    public ResponseEntity<String> register(@RequestBody CampaignRequestDto dto) {
        Long foundationNo = 1L;

        campaignService.registerCampaign(dto, foundationNo);

        log.info("캠페인 등록 신청 성공");
        return ResponseEntity.ok("캠페인 등록 신청 완료");
    }

}

package com.merge.final_project.report.finalreport.controller;

import com.merge.final_project.campaign.campaigns.entity.Campaign;
import com.merge.final_project.recipient.beneficiary.service.BeneficiaryService;
import com.merge.final_project.report.finalreport.dto.FinalReportRequestDTO;
import com.merge.final_project.report.finalreport.dto.FinalReportResponseDTO;
import com.merge.final_project.report.finalreport.service.FinalReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/final-reports")
@RequiredArgsConstructor
@Log4j2
public class
FinalReportRestController {

    private final FinalReportService finalReportService;
    private final BeneficiaryService beneficiaryService;

    /**
     * 1. 내가 참여 중인 캠페인 목록 조회 (보고서 작성 대상 확인용)
     */
    @GetMapping("/campaigns")
    public ResponseEntity<List<Campaign>> getMyCampaigns(Principal principal) {
        return ResponseEntity.ok(beneficiaryService.getMyCampaigns(principal.getName()));
    }

    /**
     * 2. 내가 작성한 보고서 목록 조회
     */
    @GetMapping("/me")
    public ResponseEntity<List<FinalReportResponseDTO>> getMyReports(Principal principal) {
        return ResponseEntity.ok(finalReportService.getMyReports(principal.getName()));
    }

    /**
     * 3. 보고서 제출 API
     */
    @PostMapping
    public ResponseEntity<String> submitReport(
            @RequestPart("dto") FinalReportRequestDTO dto,
            @RequestPart("files") List<MultipartFile> files,
            Principal principal) throws IOException {

        finalReportService.saveFullReport(dto, files, dto.getPurposes(), principal.getName());
        return ResponseEntity.ok("success");
    }

    /**
     * 4. 보고서 상세 조회 API
     */
    @GetMapping("/{reportNo}")
    public ResponseEntity<FinalReportResponseDTO> getReportDetail(
            @PathVariable("reportNo") Long reportNo,
            Principal principal) {

        return ResponseEntity.ok(finalReportService.getReportDetail(reportNo, principal.getName()));
    }

    /**
     * 5. 보고서 수정 API
     */
    @PutMapping("/{reportNo}")
    public ResponseEntity<String> updateReport(
            @PathVariable("reportNo") Long reportNo,
            @RequestPart("dto") FinalReportRequestDTO dto,
            @RequestPart(value = "files", required = false) List<MultipartFile> files,
            Principal principal) throws IOException {

        finalReportService.updateReport(reportNo, dto, files, dto.getPurposes(), principal.getName());
        return ResponseEntity.ok("success");
    }

    /**
     * 6. 캠페인 번호로 보고서 조회 (공개용)
     */
    @GetMapping("/campaign/{campaignNo}")
    public ResponseEntity<FinalReportResponseDTO> getReportByCampaign(@PathVariable("campaignNo") Long campaignNo) {
        return ResponseEntity.ok(finalReportService.getPublicReportByCampaign(campaignNo));
    }
}

package com.merge.final_project.report.finalreport.controller;

import com.merge.final_project.campaign.campaigns.entity.Campaign;
import com.merge.final_project.recipient.beneficiary.service.BeneficiaryService;
import com.merge.final_project.report.finalreport.dto.FinalReportRequestDTO;
import com.merge.final_project.report.finalreport.dto.FinalReportResponseDTO;
import com.merge.final_project.report.finalreport.service.FinalReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/finalReport")
@Log4j2
@RequiredArgsConstructor
public class FinalReportController {

    private final FinalReportService finalReportService;
    private final BeneficiaryService beneficiaryService;

    @PostMapping("/submit")
    @ResponseBody
    public ResponseEntity<String> submitReport(
            @RequestPart("dto") FinalReportRequestDTO dto,
            @RequestPart("files") List<MultipartFile> files,
            java.security.Principal principal) throws IOException {

        if (principal == null) {
            return ResponseEntity.status(401).body("로그인 정보가 없습니다.");
        }

        String email = principal.getName();
        // 💡 DTO 내부에서 purposes 리스트를 꺼내서 서비스에 전달
        finalReportService.saveFullReport(dto, files, dto.getPurposes(), email);

        return ResponseEntity.ok("보고서와 사진이 성공적으로 제출되었습니다.");
    }


    @GetMapping({"/", "/list"})
    public String list(Model model, Principal principal) {
        if (principal == null) {
            return "redirect:/api/beneficiary/test/login";
        }

        String email = principal.getName();
        log.info("수혜자 캠페인 리스트 조회 중: {}", email);

        // 1. 참여 중인 캠페인 목록 가져오기
        List<Campaign> myCampaigns = beneficiaryService.getMyCampaigns(email);

        // 2. 각 캠페인별로 작성된 보고서가 있는지 확인 (보고서 번호 맵핑)
        // [수정] 템플릿에서 편하게 쓰기 위해 DTO나 별도의 맵을 활용할 수 있지만, 
        // 여기서는 서비스에서 처리된 목록을 가져오는 방식으로 가겠습니다.
        List<FinalReportResponseDTO> myReports = finalReportService.getMyReports(email);

        model.addAttribute("campaignList", myCampaigns);
        model.addAttribute("reportList", myReports); // 이미 작성된 보고서 목록

        return "finalReport/list";
    }

    @GetMapping("/submit")
    public String submitForm(@RequestParam("campaignNo") Long campaignNo, Model model) {
        log.info("보고서 작성 페이지 이동 - 캠페인 번호: {}", campaignNo);
        model.addAttribute("campaignNo", campaignNo);
        return "finalReport/submit"; 
    }

    @GetMapping("/{reportNo}")
    public String getReportDetail(
            @PathVariable("reportNo") Long reportNo,
            java.security.Principal principal,
            org.springframework.ui.Model model) {

        if (principal == null) {
            return "redirect:/api/beneficiary/test/login";
        }

        // 서비스에서 본인 확인 후 상세 정보를 가져옵니다.
        FinalReportResponseDTO detail = finalReportService.getReportDetail(reportNo, principal.getName());
        model.addAttribute("report", detail);

        return "finalReport/detail";
    }

    /**
     * 내가 쓴 보고서 목록 조회 API
     */
    @GetMapping("/my")
    @ResponseBody
    public ResponseEntity<?> getMyReports(java.security.Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(401).body("로그인 정보가 없습니다.");
        }

        List<FinalReportResponseDTO> myReports = finalReportService.getMyReports(principal.getName());
        return ResponseEntity.ok(myReports);
    }
    @PutMapping("/update/{reportNo}")
    public ResponseEntity<String> updateReport(
            @PathVariable("reportNo") Long reportNo,
            @RequestPart("dto") FinalReportRequestDTO dto,
            @RequestPart(value = "files", required = false) List<MultipartFile> files,
            java.security.Principal principal) throws IOException {

        if (principal == null) {
            return ResponseEntity.status(401).body("로그인 정보가 없습니다.");
        }

        finalReportService.updateReport(reportNo, dto, files, dto.getPurposes(), principal.getName());
        return ResponseEntity.ok("보고서 수정 완료");
    }
    // 2. 보고서 제출 테스트 페이지 연결
    @GetMapping("/test/submit")
    public String testSubmitPage() {
        return "finalReport/test-submit";
    }

    // 3. 보고서 수정 테스트 페이지 연결
    @GetMapping("/test/update")
    public String testUpdatePage(@RequestParam("reportNo") Long reportNo, 
                                java.security.Principal principal, 
                                org.springframework.ui.Model model) {
        
        if (principal == null) {
            return "redirect:/api/beneficiary/test/login";
        }

        // 기존 보고서 상세 정보를 가져와서 모델에 담습니다.
        FinalReportResponseDTO detail = finalReportService.getReportDetail(reportNo, principal.getName());
        model.addAttribute("report", detail);
        
        return "finalReport/test-update";
    }
}


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
            @RequestPart("purposes") List<String> purposes,
            java.security.Principal principal) throws IOException { // [수정] User 대신 Principal 사용

        if (principal == null) {
            return ResponseEntity.status(401).body("로그인 정보가 없습니다.");
        }

        String email = principal.getName(); // 이제 여기서 null 에러가 나지 않습니다.
        finalReportService.saveFullReport(dto, files, purposes, email);

        return ResponseEntity.ok("보고서와 사진이 성공적으로 제출되었습니다.");
    }


    @GetMapping({"/", "/list"})
    public String list(Model model, Principal principal) { // [수정] User 대신 Principal 사용
        if (principal == null) {
            return "redirect:/api/beneficiary/signin"; // 로그인 안 됐으면 로그인 페이지로
        }

        String email = principal.getName(); // 인증된 사용자의 이메일(ID) 추출
        log.info("수혜자 캠페인 리스트 조회 중: {}", email);

        List<Campaign> myCampaigns = beneficiaryService.getMyCampaigns(email);
        model.addAttribute("campaignList", myCampaigns);

        return "finalReport/list";
    }
    @GetMapping("/submit")
    @ResponseBody
    public String submitForm(@RequestParam("campaignNo") Long campaignNo, Model model) {
        log.info("보고서 작성 페이지 이동 - 캠페인 번호: {}", campaignNo);

        // 화면에 "999번 캠페인 보고서 작성"이라고 띄워주기 위해 번호를 전달합니다.
        model.addAttribute("campaignNo", campaignNo);

        return "finalReport/submit"; // src/main/resources/templates/finalReport/submit.html
    }
    @GetMapping("/{reportNo}")
    public ResponseEntity<?> getReportDetail(
            @PathVariable("reportNo") Long reportNo,
            java.security.Principal principal) {

        if (principal == null) {
            return ResponseEntity.status(401).body("로그인 정보가 없습니다.");
        }

        // 서비스에서 본인 확인 후 상세 정보를 가져옵니다.
        FinalReportResponseDTO detail = finalReportService.getReportDetail(reportNo, principal.getName());

        return ResponseEntity.ok(detail);
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
            @RequestPart(value = "purposes", required = false) List<String> purposes,
            java.security.Principal principal) throws IOException {

        if (principal == null) {
            return ResponseEntity.status(401).body("로그인 정보가 없습니다.");
        }

        finalReportService.updateReport(reportNo, dto, files, purposes, principal.getName());
        return ResponseEntity.ok("보고서 수정 완료");
    }
}


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
import java.util.List;

@Controller
@RequestMapping("/finalReport")
@Log4j2
@RequiredArgsConstructor
public class FinalReportController {

    private final FinalReportService finalReportService;
    private final BeneficiaryService beneficiaryService;

    @PostMapping("/submit")
    @ResponseBody // 💡 성공 메시지를 보내기 위해 필요합니다.
    public ResponseEntity<String> submitReport(
            @RequestPart("dto") FinalReportRequestDTO dto,           // 1. JSON 데이터
            @RequestPart("files") List<MultipartFile> files,        // 2. 이미지 파일 리스트
            @RequestPart("purposes") List<String> purposes,
            @AuthenticationPrincipal User user) throws IOException { // 3. 로그인 정보


        // 캡슐화된 서비스 호출 (데이터 + 파일 + 이메일)
        finalReportService.saveFullReport(dto, files,purposes, user.getUsername());

        return ResponseEntity.ok("보고서와 사진이 성공적으로 제출되었습니다.");
    }


    @GetMapping({"/", "/list"})

    public String list(Model model, @AuthenticationPrincipal User user) {
        log.info("수혜자 캠페인 리스트 조회 중: {}", user.getUsername());

        // 1. 로그인한 이메일로 수혜자 정보 및 해당 캠페인 리스트 가져오기
        List<Campaign> myCampaigns = beneficiaryService.getMyCampaigns(user.getUsername());

        // 2. 화면(HTML)으로 데이터 전달
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
    public ResponseEntity<FinalReportResponseDTO> getReportDetail(
            @PathVariable("reportNo") Long reportNo) {

        // 서비스에서 특정 보고서 하나를 DTO로 변환해 가져옵니다.
        FinalReportResponseDTO detail = finalReportService.getReportDetail(reportNo);

        return ResponseEntity.ok(detail);
    }
    @PutMapping("/update/{reportNo}")
    public ResponseEntity<String> updateReport(
            @PathVariable("reportNo") Long reportNo,
            @RequestPart("dto") FinalReportRequestDTO dto,
            @RequestPart(value = "files", required = false) List<MultipartFile> files,
            @RequestPart(value = "purposes", required = false) List<String> purposes) throws IOException {

        finalReportService.updateReport(reportNo, dto, files, purposes);
        return ResponseEntity.ok("보고서 수정 완료");
    }
}


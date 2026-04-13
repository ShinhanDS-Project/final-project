package com.merge.final_project.report.finalreport.controller;

import com.merge.final_project.campaign.campaigns.entity.Campaign;
import com.merge.final_project.recipient.beneficiary.service.BeneficiaryService;
import com.merge.final_project.report.finalreport.dto.FinalReportRequestDTO;
import com.merge.final_project.report.finalreport.dto.FinalReportResponseDTO;
import com.merge.final_project.report.finalreport.service.FinalReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.Principal;
import java.util.List;

import com.merge.final_project.blockchain.dto.BlockchainWalletDetailResponse;
import com.merge.final_project.blockchain.service.BlockchainDashboardQueryService;
import com.merge.final_project.recipient.beneficiary.entity.Beneficiary;

@Controller
@RequestMapping("/finalReport")
@Log4j2
@RequiredArgsConstructor
public class FinalReportController {

    private final FinalReportService finalReportService;
    private final BeneficiaryService beneficiaryService;
    private final BlockchainDashboardQueryService blockchainDashboardQueryService; // 💡 지갑 조회 서비스 주입

    @GetMapping({"/", "/list"})
    public String list(Model model, Principal principal) {
        if (principal == null) {
            return "redirect:/api/beneficiary/signin";
        }

        String email = principal.getName();
        
        // 1. 수혜자 정보 및 참여 캠페인 목록 가져오기
        Beneficiary beneficiary = beneficiaryService.getBeneficiaryByEmail(email);
        List<Campaign> myCampaigns = beneficiaryService.getMyCampaigns(email);
        List<FinalReportResponseDTO> myReports = finalReportService.getMyReports(email);

        // 2. 💡 지갑 정보(주소, 잔액) 조회
        if (beneficiary.getWallet() != null) {
            String walletAddress = beneficiary.getWallet().getWalletAddress();
            // 지갑 상세 조회 (페이지 1, 성공한 트랜잭션 기준)
            BlockchainWalletDetailResponse walletDetail = 
                blockchainDashboardQueryService.getWalletDetail(walletAddress, 1, "SUCCESS");
            
            model.addAttribute("wallet", walletDetail.wallet());
            model.addAttribute("beneficiaryNo", beneficiary.getBeneficiaryNo());
        }

        model.addAttribute("campaignList", myCampaigns);
        model.addAttribute("reportList", myReports);

        return "finalReport/list";
    }

    @GetMapping("/submit")
    public String submitForm(@RequestParam("campaignNo") Long campaignNo, Model model, Principal principal) {
        if (principal == null) return "redirect:/api/beneficiary/signin";
        
        model.addAttribute("campaignNo", campaignNo);
        return "finalReport/submit"; 
    }

    @PostMapping("/submit")
    @ResponseBody
    public ResponseEntity<String> submitReport(
            @RequestPart("dto") FinalReportRequestDTO dto,
            @RequestPart("files") List<MultipartFile> files,
            Principal principal) throws IOException {

        if (principal == null) return ResponseEntity.status(401).body("로그인 필요");

        finalReportService.saveFullReport(dto, files, dto.getPurposes(), principal.getName());
        return ResponseEntity.ok("제출 완료");
    }

    @GetMapping("/{reportNo}")
    public String getReportDetail(
            @PathVariable("reportNo") Long reportNo,
            Principal principal,
            Model model) {

        if (principal == null) return "redirect:/api/beneficiary/signin";

        FinalReportResponseDTO detail = finalReportService.getReportDetail(reportNo, principal.getName());
        model.addAttribute("report", detail);

        return "finalReport/detail";
    }

    @GetMapping("/update")
    public String updatePage(@RequestParam("reportNo") Long reportNo, 
                             Principal principal, 
                             Model model) {
        
        if (principal == null) return "redirect:/api/beneficiary/signin";

        FinalReportResponseDTO detail = finalReportService.getReportDetail(reportNo, principal.getName());
        model.addAttribute("report", detail);
        
        return "finalReport/update";
    }

    @PutMapping("/update/{reportNo}")
    @ResponseBody
    public ResponseEntity<String> updateReport(
            @PathVariable("reportNo") Long reportNo,
            @RequestPart("dto") FinalReportRequestDTO dto,
            @RequestPart(value = "files", required = false) List<MultipartFile> files,
            Principal principal) throws IOException {

        if (principal == null) return ResponseEntity.status(401).body("로그인 필요");

        finalReportService.updateReport(reportNo, dto, files, dto.getPurposes(), principal.getName());
        return ResponseEntity.ok("수정 완료");
    }

    @GetMapping("/my")
    @ResponseBody
    public ResponseEntity<?> getMyReports(Principal principal) {
        if (principal == null) return ResponseEntity.status(401).body("로그인 필요");
        return ResponseEntity.ok(finalReportService.getMyReports(principal.getName()));
    }
}

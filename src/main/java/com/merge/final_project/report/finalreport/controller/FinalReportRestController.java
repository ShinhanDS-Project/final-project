package com.merge.final_project.report.finalreport.controller;

import com.merge.final_project.campaign.campaigns.entity.Campaign;
import com.merge.final_project.recipient.beneficiary.service.BeneficiaryService;
import com.merge.final_project.report.finalreport.dto.FinalReportRequestDTO;
import com.merge.final_project.report.finalreport.dto.FinalReportResponseDTO;
import com.merge.final_project.report.finalreport.service.FinalReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.Principal;
import java.util.List;

@io.swagger.v3.oas.annotations.tags.Tag(name = "활동 보고서", description = "수혜자 활동 보고서 조회·제출·수정 API")
@RestController
@RequestMapping("/api/v1/final-reports")
@RequiredArgsConstructor
@Log4j2
public class FinalReportRestController {

    private final FinalReportService finalReportService;
    private final BeneficiaryService beneficiaryService;

    @Operation(summary = "내 참여 캠페인 목록 조회", description = "수혜자가 참여 중인 캠페인 목록을 조회합니다. 보고서 작성 대상 캠페인 확인용입니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 요청")
    })
    @GetMapping("/campaigns")
    public ResponseEntity<List<Campaign>> getMyCampaigns(Principal principal) {
        return ResponseEntity.ok(beneficiaryService.getMyCampaigns(principal.getName()));
    }

    @Operation(summary = "내 보고서 목록 조회", description = "로그인한 수혜자가 작성한 활동 보고서 목록을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 요청")
    })
    @GetMapping("/me")
    public ResponseEntity<List<FinalReportResponseDTO>> getMyReports(Principal principal) {
        return ResponseEntity.ok(finalReportService.getMyReports(principal.getName()));
    }

    @Operation(summary = "활동 보고서 제출", description = "수혜자가 활동 보고서를 제출합니다. "
            + "multipart/form-data: dto 파트(JSON)와 files 파트(첨부 파일 목록)를 함께 전송합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "제출 성공"),
            @ApiResponse(responseCode = "400", description = "요청 값 유효성 오류"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 요청"),
            @ApiResponse(responseCode = "409", description = "이미 제출된 보고서")
    })
    @PostMapping
    public ResponseEntity<String> submitReport(
            @Parameter(description = "보고서 정보 (JSON)", required = true)
            @RequestPart("dto") FinalReportRequestDTO dto,
            @Parameter(description = "첨부 파일 목록", required = true)
            @RequestPart("files") List<MultipartFile> files,
            Principal principal) throws IOException {

        finalReportService.saveFullReport(dto, files, dto.getPurposes(), principal.getName());
        return ResponseEntity.ok("success");
    }

    @Operation(summary = "활동 보고서 상세 조회", description = "보고서 번호로 활동 보고서 상세 내용을 조회합니다. 본인 보고서만 조회 가능합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "403", description = "본인 보고서가 아님"),
            @ApiResponse(responseCode = "404", description = "보고서를 찾을 수 없음"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 요청")
    })
    @GetMapping("/{reportNo}")
    public ResponseEntity<FinalReportResponseDTO> getReportDetail(
            @Parameter(description = "보고서 번호", example = "1") @PathVariable("reportNo") Long reportNo,
            Principal principal) {

        return ResponseEntity.ok(finalReportService.getReportDetail(reportNo, principal.getName()));
    }

    @Operation(summary = "활동 보고서 수정", description = "제출한 활동 보고서를 수정합니다. 관리자 승인 전에만 수정 가능합니다. "
            + "multipart/form-data: dto 파트(JSON)와 files 파트(첨부 파일, 선택)를 함께 전송합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "수정 성공"),
            @ApiResponse(responseCode = "400", description = "요청 값 유효성 오류"),
            @ApiResponse(responseCode = "403", description = "본인 보고서가 아니거나 이미 승인된 보고서"),
            @ApiResponse(responseCode = "404", description = "보고서를 찾을 수 없음"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 요청")
    })
    @PutMapping("/{reportNo}")
    public ResponseEntity<String> updateReport(
            @Parameter(description = "보고서 번호", example = "1") @PathVariable("reportNo") Long reportNo,
            @Parameter(description = "수정할 보고서 정보 (JSON)", required = true)
            @RequestPart("dto") FinalReportRequestDTO dto,
            @Parameter(description = "첨부 파일 목록 (선택, 생략 시 기존 파일 유지)")
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

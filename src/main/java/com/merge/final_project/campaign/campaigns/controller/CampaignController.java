package com.merge.final_project.campaign.campaigns.controller;

import com.merge.final_project.campaign.campaigns.dto.CampaignBeneficiaryCheckResponseDTO;
import com.merge.final_project.campaign.campaigns.dto.CampaignDetailResponseDTO;
import com.merge.final_project.campaign.campaigns.dto.CampaignFoundationCheckResponseDTO;
import com.merge.final_project.campaign.campaigns.dto.CampaignListResponseDTO;
import com.merge.final_project.campaign.campaigns.dto.CampaignRegisterResponseDTO;
import com.merge.final_project.campaign.campaigns.dto.CampaignRequestDTO;
import com.merge.final_project.campaign.campaigns.service.CampaignService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
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

@Tag(name = "캠페인", description = "캠페인 목록 조회·등록·상세 조회 API")
@Controller
@RequestMapping("/api/foundation/campaigns")
@Log4j2
@RequiredArgsConstructor
public class CampaignController {
    private final CampaignService campaignService;

    @Operation(summary = "캠페인 목록 조회 (공개)", description = "승인된 캠페인 목록을 정렬·검색 조건·카테고리로 필터링하여 조회합니다. 인증 불필요.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공")
    })
    @ResponseBody
    @GetMapping
    public ResponseEntity<List<CampaignListResponseDTO>> getCampaignList(
            @Parameter(description = "정렬 기준 (deadline: 마감 임박순, latest: 최신순)", example = "deadline")
            @RequestParam(value = "sort", defaultValue = "deadline") String sort,
            @Parameter(description = "검색 유형 (title: 제목, foundation: 단체명)", example = "title")
            @RequestParam(value = "searchType", required = false) String searchType,
            @Parameter(description = "검색 키워드", example = "어린이")
            @RequestParam(value = "keyword", required = false) String keyword,
            @Parameter(description = "카테고리 필터 (CHILD, ELDERLY, DISABLED, ANIMAL, ENVIRONMENT 등)", example = "CHILD")
            @RequestParam(value = "category", required = false) String category
    ) {
        return ResponseEntity.ok(campaignService.getCampaignList(sort, searchType, keyword, category));
    }

    @Operation(summary = "캠페인 등록", description = "로그인한 기부단체가 새 캠페인을 등록합니다. 관리자 승인 전까지 PENDING 상태입니다. "
            + "multipart/form-data: dto 파트(JSON)와 imageFile(대표 이미지), detailImageFiles(상세 이미지, 선택)를 함께 전송합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "등록 성공"),
            @ApiResponse(responseCode = "400", description = "요청 값 유효성 오류"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 요청"),
            @ApiResponse(responseCode = "403", description = "기부단체 권한 없음")
    })
    @ResponseBody
    @PostMapping(value = "/register", consumes = {"multipart/form-data"})
    public ResponseEntity<CampaignRegisterResponseDTO> register(
            @Parameter(description = "캠페인 등록 정보 (JSON)", required = true)
            @RequestPart("dto") CampaignRequestDTO dto,
            @Parameter(description = "캠페인 대표 이미지", required = true)
            @RequestPart("imageFile") MultipartFile imageFile,
            @Parameter(description = "캠페인 상세 이미지 목록 (선택)")
            @RequestPart(value = "detailImageFiles", required = false) List<MultipartFile> detailImageFiles,
            Authentication authentication
    ) {
        Long foundationNo = (Long) authentication.getDetails();
        return ResponseEntity.ok(campaignService.registerCampaign(dto, imageFile, detailImageFiles, foundationNo));
    }

    @Operation(summary = "수혜자 엔트리코드 확인", description = "캠페인 등록 시 입력한 엔트리 코드가 유효한 수혜자 코드인지 검증합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "확인 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 요청")
    })
    @ResponseBody
    @GetMapping("/beneficiary-check")
    public ResponseEntity<CampaignBeneficiaryCheckResponseDTO> checkBeneficiary(
            @Parameter(description = "수혜자 엔트리 코드", required = true, example = "ENT-20240101-001")
            @RequestParam String entryCode) {
        return ResponseEntity.ok(campaignService.checkBeneficiaryByEntryCode(entryCode));
    }

    @Operation(summary = "기부단체 지갑 상태 확인", description = "캠페인 등록 전 기부단체의 블록체인 지갑 연동 여부와 상태를 확인합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "확인 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 요청")
    })
    @ResponseBody
    @GetMapping("/foundation-check")
    public ResponseEntity<CampaignFoundationCheckResponseDTO> checkFoundation(
            @Parameter(description = "기부단체 번호", required = true, example = "1")
            @RequestParam Long foundationNo) {
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

    @Operation(summary = "캠페인 상세 조회 (REST)", description = "캠페인 상세 정보를 JSON으로 반환합니다. 인증 불필요.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "캠페인을 찾을 수 없음")
    })
    @ResponseBody
    @GetMapping("/{campaignNo}/detail")
    public ResponseEntity<CampaignDetailResponseDTO> getCampaignDetail(
            @Parameter(description = "캠페인 번호", example = "1") @PathVariable Long campaignNo) {
        return ResponseEntity.ok(campaignService.getCampaignDetail(campaignNo));
    }
}


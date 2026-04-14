package com.merge.final_project.campaign.campaigns.controller;

import com.merge.final_project.campaign.campaigns.dto.CampaignBeneficiaryCheckResponseDTO;
import com.merge.final_project.campaign.campaigns.dto.CampaignDetailResponseDTO;
import com.merge.final_project.campaign.campaigns.dto.CampaignFoundationCheckResponseDTO;
import com.merge.final_project.campaign.campaigns.dto.CampaignListResponseDTO;
import com.merge.final_project.campaign.campaigns.dto.CampaignRegisterResponseDTO;
import com.merge.final_project.campaign.campaigns.dto.CampaignRequestDTO;
import com.merge.final_project.campaign.campaigns.service.CampaignService;
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

@Controller
@RequestMapping("/api/foundation/campaigns")
@Log4j2
@RequiredArgsConstructor
public class CampaignController {
    private final CampaignService campaignService;

    /* 캠페인 목록을 가져오는 API: 정렬, 검색조건, 키워드, 카테고리를 파라미터로 받아 필터링된 리스트를 반환함 */
    @ResponseBody
    @GetMapping
    public ResponseEntity<List<CampaignListResponseDTO>> getCampaignList(
            @RequestParam(value = "sort", defaultValue = "deadline") String sort,
            @RequestParam(value = "searchType", required = false) String searchType,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "category", required = false) String category
    ) {
        return ResponseEntity.ok(campaignService.getCampaignList(sort, searchType, keyword, category));
    }

    /* 신규 캠페인 등록 API: JSON 데이터(@RequestPart dto)와 이미지 파일들을 동시에 처리하는 multipart/form-data 방식 */
    // [가빈] 로그인 되어 있는 기부단체만 등록할 수 있도록, foundationNo를 jwt 토큰의 claim으로 추출하는 방식으로 변경
    @ResponseBody
    @PostMapping(value = "/register", consumes = {"multipart/form-data"})
    public ResponseEntity<CampaignRegisterResponseDTO> register(
            @RequestPart("dto") CampaignRequestDTO dto,
            @RequestPart("imageFile") MultipartFile imageFile,
            @RequestPart(value = "detailImageFiles", required = false) List<MultipartFile> detailImageFiles,
            Authentication authentication
    ) {
        Long foundationNo = (Long) authentication.getDetails();
        return ResponseEntity.ok(campaignService.registerCampaign(dto, imageFile, detailImageFiles, foundationNo));
    }

    /* 수혜자 확인: 입력된 엔트리 코드가 유효한 수혜자인지 검증함 */
    @ResponseBody
    @GetMapping("/beneficiary-check")
    public ResponseEntity<CampaignBeneficiaryCheckResponseDTO> checkBeneficiary(@RequestParam String entryCode) {
        return ResponseEntity.ok(campaignService.checkBeneficiaryByEntryCode(entryCode));
    }

    /* 기부단체 확인: 특정 재단의 지갑 상태나 권한 정보를 확인함 */
    @ResponseBody
    @GetMapping("/foundation-check")
    public ResponseEntity<CampaignFoundationCheckResponseDTO> checkFoundation(@RequestParam Long foundationNo) {
        return ResponseEntity.ok(campaignService.checkFoundationWalletStatus(foundationNo));
    }

    /* 화면 이동: 캠페인 등록 페이지(HTML)를 호출함 */
    @GetMapping("/register")
    public String getRegisterPage() {
        return "campaign/register";
    }

    /* 화면 이동: 캠페인 목록 페이지(HTML)를 호출함 */
    @GetMapping("/list")
    public String getCampaignListPage() {
        return "campaign/list";
    }

    /* 화면 이동: 특정 캠페인의 상세 정보를 조회하여 Model에 담고 상세 페이지(HTML)로 이동함 */
    @GetMapping("/{campaignNo}")
    public String getCampaignDetailPage(@PathVariable Long campaignNo, Model model) {
        CampaignDetailResponseDTO campaign = campaignService.getCampaignDetail(campaignNo);
        model.addAttribute("campaign", campaign);
        return "campaign/detail";
    }

    @ResponseBody
    @GetMapping("/{campaignNo}/detail")
    public ResponseEntity<CampaignDetailResponseDTO> getCampaignDetail(@PathVariable Long campaignNo) {
        return ResponseEntity.ok(campaignService.getCampaignDetail(campaignNo));
    }
}


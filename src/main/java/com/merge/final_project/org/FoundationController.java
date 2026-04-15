package com.merge.final_project.org;

import com.merge.final_project.campaign.campaigns.CampaignStatus;
import com.merge.final_project.campaign.campaigns.dto.CampaignListResponseDTO;
import com.merge.final_project.org.dto.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/foundation")
@RequiredArgsConstructor
public class FoundationController {

    private final FoundationService foundationService;

    // 사업자등록번호 중복 체크 (가입 신청 전 선행 확인)
    @GetMapping("/check-brn")
    public ResponseEntity<Boolean> checkBusinessRegistrationNumber(
            @RequestParam String businessRegistrationNumber) {
        return ResponseEntity.ok(
                foundationService.existByBusinessRegistrationNumber(businessRegistrationNumber)
        );
    }

    // 비회원 기부단체 가입 신청 (승인/반려 전)
    @PostMapping(value = "/signup", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<FoundationApplyResponseDTO> apply(@RequestPart("data") @Valid FoundationApplyRequestDTO requestDTO,
                                      @RequestPart(value = "profileImage", required = false) MultipartFile profileImage) {
        return ResponseEntity.ok(foundationService.apply(requestDTO, profileImage));
    }

    // 승인 완료된 기부단체 리스트 조회 (사용자용 — 키워드 검색, 페이징, 기본: 최신순)
    @GetMapping("/all")
    public ResponseEntity<Page<FoundationListResponseDTO>> getApprovedList(
            @RequestParam(defaultValue = "") String keyword,
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(foundationService.getPublicFoundationList(keyword, pageable));
    }

    // 기부단체 상세 조회 (일반 사용자 공개 — ACTIVE 단체만, 민감 정보 제외)
    @GetMapping("/{foundationNo}")
    public ResponseEntity<FoundationPublicDetailDTO> getDetail(@PathVariable Long foundationNo) {
        return ResponseEntity.ok(foundationService.getPublicFoundationDetail(foundationNo));
    }

    // 기부단체 로그인
    @PostMapping("/login")
    public ResponseEntity<FoundationSigninResponseDTO> login(@RequestBody @Valid FoundationSigninRequestDTO requestDTO) {
        return ResponseEntity.ok(foundationService.login(requestDTO));
    }

    // 기부단체 로그아웃 (프론트에서 토큰 제거, 서버는 로그만 남김)
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestHeader("Authorization") String bearerToken) {
        foundationService.logout(bearerToken);
        return ResponseEntity.ok().build();
    }

    // 기부단체 본인 상세 정보 조회 (마이페이지 / 회원정보 수정 화면 진입 시)
    @GetMapping("/me")
    public ResponseEntity<FoundationDetailResponseDTO> getMyDetail(Authentication authentication) {
        Long foundationNo = (Long) authentication.getDetails();
        return ResponseEntity.ok(foundationService.getFoundationDetail(foundationNo));
    }

    // 기부단체 회원정보 수정 (설명, 연락처, 계좌, 은행명, 수수료율, 프로필 이미지)
    @PatchMapping(value = "/me", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<FoundationDetailResponseDTO> updateMyInfo(
            Authentication authentication,
            @RequestPart("data") @Valid FoundationUpdateRequestDTO requestDTO,
            @RequestPart(value = "profileImage", required = false) MultipartFile profileImage) {
        Long foundationNo = (Long) authentication.getDetails();
        return ResponseEntity.ok(foundationService.updateFoundationInfo(foundationNo, requestDTO, profileImage));
    }

    // 기부단체 비밀번호 변경
    @PatchMapping("/me/password")
    public ResponseEntity<Void> updateMyPassword(
            Authentication authentication,
            @RequestBody @Valid FoundationPasswordUpdateRequestDTO requestDTO) {
        Long foundationNo = (Long) authentication.getDetails();
        foundationService.updateFoundationPassword(foundationNo, requestDTO);
        return ResponseEntity.ok().build();
    }

    //기부단체 캠페인 리스트 조회 (기본: 최신순, 공개)
    @GetMapping("/{foundationNo}/campaigns")
    public ResponseEntity<Page<CampaignListResponseDTO>> getMyCampaigns(
            @PathVariable Long foundationNo,
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(foundationService.getMyCampaigns(foundationNo, pageable));
    }

    // 기부단체 마이페이지 — 상태 필터 + 키워드 검색 캠페인 목록 (기본: 최신순)
    @GetMapping("/me/campaigns/filter")
    public ResponseEntity<Page<FoundationMyCampaignDTO>> getMyCampaignsWithFilter(
            Authentication authentication,
            @RequestParam(required = false) CampaignStatus campaignStatus,
            @RequestParam(defaultValue = "") String keyword,
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Long foundationNo = (Long) authentication.getDetails();
        return ResponseEntity.ok(foundationService.getMyCampaignsWithFilter(foundationNo, campaignStatus, keyword, pageable));
    }

    // 기부단체 마이페이지 — 진행 중 캠페인 수 + 이번달 모금액
    @GetMapping("/me/stats")
    public ResponseEntity<FoundationMyPageStatsDTO> getMyPageStats(Authentication authentication) {
        Long foundationNo = (Long) authentication.getDetails();
        return ResponseEntity.ok(foundationService.getMyPageStats(foundationNo));
    }

    // 기부단체 지갑 주소 + 잔액 조회 (공개)
    @GetMapping("/{foundationNo}/wallet")
    public ResponseEntity<FoundationWalletDTO> getMyWalletInfo(@PathVariable Long foundationNo) {
        return ResponseEntity.ok(foundationService.getMyWalletInfo(foundationNo));
    }

    // 기부단체 마이페이지 — 정산 내역 (기본: 최신순)
    @GetMapping("/me/settlements")
    public ResponseEntity<Page<FoundationSettlementDTO>> getMySettlements(
            Authentication authentication,
            @PageableDefault(sort = "settledAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Long foundationNo = (Long) authentication.getDetails();
        return ResponseEntity.ok(foundationService.getMySettlements(foundationNo, pageable));
    }

    // 기부단체 마이페이지 — 환금(현금화) 내역 (기본: 최신순)
    @GetMapping("/me/redemptions")
    public ResponseEntity<Page<FoundationRedemptionDTO>> getMyRedemptions(
            Authentication authentication,
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Long foundationNo = (Long) authentication.getDetails();
        return ResponseEntity.ok(foundationService.getMyRedemptions(foundationNo, pageable));
    }
}

package com.merge.final_project.org;

import com.merge.final_project.campaign.campaigns.CampaignStatus;
import com.merge.final_project.campaign.campaigns.dto.CampaignListResponseDTO;
import com.merge.final_project.org.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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

@Tag(name = "기부단체", description = "기부단체 가입·로그인·마이페이지·캠페인·정산·환금 API")
@RestController
@RequestMapping("/api/foundation")
@RequiredArgsConstructor
public class FoundationController {

    private final FoundationService foundationService;

    // 사업자등록번호 중복 체크 (가입 신청 전 선행 확인)
    @GetMapping("/check-brn")
    public ResponseEntity<Boolean> checkBusinessRegistrationNumber(
            @Parameter(description = "사업자등록번호 (하이픈 제외)", example = "1234567890", required = true)
            @RequestParam String businessRegistrationNumber) {
        return ResponseEntity.ok(
                foundationService.existByBusinessRegistrationNumber(businessRegistrationNumber)
        );
    }

    @Operation(summary = "기부단체 가입 신청", description = "비회원 기부단체가 가입을 신청합니다. 관리자 승인 전까지 PENDING 상태입니다. "
            + "multipart/form-data: data 파트(JSON)와 profileImage 파트(이미지, 선택)를 함께 전송합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "가입 신청 성공"),
            @ApiResponse(responseCode = "400", description = "요청 값 유효성 오류"),
            @ApiResponse(responseCode = "409", description = "이미 등록된 이메일 또는 사업자등록번호")
    })
    @PostMapping(value = "/signup", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<FoundationApplyResponseDTO> apply(
            @Parameter(description = "가입 신청 정보 (JSON)", required = true)
            @RequestPart("data") @Valid FoundationApplyRequestDTO requestDTO,
            @Parameter(description = "프로필 이미지 (선택)")
            @RequestPart(value = "profileImage", required = false) MultipartFile profileImage) {
        return ResponseEntity.ok(foundationService.apply(requestDTO, profileImage));
    }

    @Operation(summary = "기부단체 목록 조회 (공개)", description = "승인 완료된 기부단체 목록을 키워드 검색과 페이징으로 조회합니다. 인증 불필요.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공")
    })
    @GetMapping("/all")
    public ResponseEntity<Page<FoundationListResponseDTO>> getApprovedList(
            @Parameter(description = "단체명 키워드 검색", example = "초록")
            @RequestParam(defaultValue = "") String keyword,
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(foundationService.getPublicFoundationList(keyword, pageable));
    }

    // 기부단체 공개 상세 조회 — 인증 불필요, ACTIVE 단체만, 이메일·계좌·상태값 제외
    @Operation(summary = "기부단체 상세 조회 (공개)",
            description = "일반 사용자용 기부단체 상세 정보를 조회합니다. 인증 불필요. "
                    + "ACTIVE 상태 단체만 조회 가능하며, 이메일·계좌번호·심사 상태 등 민감 정보는 포함되지 않습니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "기부단체를 찾을 수 없음 (비활성화 단체 포함)")
    })
    @GetMapping("/{foundationNo}")
    public ResponseEntity<FoundationPublicDetailDTO> getDetail(
            @Parameter(description = "기부단체 번호", example = "1") @PathVariable Long foundationNo) {
        return ResponseEntity.ok(foundationService.getPublicFoundationDetail(foundationNo));
    }

    // 기부단체 본인 상세 정보 조회 — ROLE_FOUNDATION 필요, 이메일·계좌 등 전체 정보 포함
    @Operation(summary = "내 기부단체 정보 조회 (마이페이지)",
            description = "로그인한 기부단체 본인의 전체 상세 정보를 조회합니다. 회원정보 수정 화면 진입 시 사용합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 요청"),
            @ApiResponse(responseCode = "403", description = "기부단체 권한 없음")
    })
    @GetMapping("/me")
    public ResponseEntity<FoundationDetailResponseDTO> getMyDetail(Authentication authentication) {
        Long foundationNo = (Long) authentication.getDetails();
        return ResponseEntity.ok(foundationService.getFoundationDetail(foundationNo));
    }

    @Operation(summary = "기부단체 로그인", description = "기부단체 이메일과 비밀번호로 로그인하여 JWT 액세스 토큰을 발급합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "로그인 성공 — JWT 토큰 반환"),
            @ApiResponse(responseCode = "401", description = "이메일 또는 비밀번호 불일치"),
            @ApiResponse(responseCode = "400", description = "요청 값 유효성 오류")
    })
    @PostMapping("/login")
    public ResponseEntity<FoundationSigninResponseDTO> login(
            @RequestBody @Valid FoundationSigninRequestDTO requestDTO) {
        return ResponseEntity.ok(foundationService.login(requestDTO));
    }

    @Operation(summary = "기부단체 로그아웃", description = "Authorization 헤더의 Bearer 토큰을 무효화합니다. 프론트에서도 토큰을 제거해야 합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "로그아웃 성공"),
            @ApiResponse(responseCode = "401", description = "유효하지 않은 토큰")
    })
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @Parameter(description = "Bearer {accessToken}", required = true)
            @RequestHeader("Authorization") String bearerToken) {
        foundationService.logout(bearerToken);
        return ResponseEntity.ok().build();
    }

    // 기부단체 회원정보 수정 (설명, 연락처, 계좌, 은행명, 수수료율, 프로필 이미지)
    @Operation(summary = "기부단체 회원정보 수정", description = "로그인한 기부단체의 설명·연락처·계좌·은행명·수수료율·프로필 이미지를 수정합니다. "
            + "multipart/form-data: data 파트(JSON)와 profileImage 파트(이미지, 선택)를 함께 전송합니다.")
    @ApiResponses({r

            @ApiResponse(responseCode = "200", description = "수정 성공"),
            @ApiResponse(responseCode = "400", description = "요청 값 유효성 오류"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 요청"),
            @ApiResponse(responseCode = "403", description = "기부단체 권한 없음")
    })
    @PatchMapping(value = "/me", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<FoundationDetailResponseDTO> updateMyInfo(
            Authentication authentication,
            @Parameter(description = "수정 정보 (JSON)", required = true)
            @RequestPart("data") @Valid FoundationUpdateRequestDTO requestDTO,
            @Parameter(description = "프로필 이미지 (선택)")
            @RequestPart(value = "profileImage", required = false) MultipartFile profileImage) {
        Long foundationNo = (Long) authentication.getDetails();
        return ResponseEntity.ok(foundationService.updateFoundationInfo(foundationNo, requestDTO, profileImage));
    }

    @Operation(summary = "기부단체 비밀번호 변경", description = "로그인한 기부단체의 비밀번호를 변경합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "비밀번호 변경 성공"),
            @ApiResponse(responseCode = "400", description = "현재 비밀번호 불일치 또는 요청 값 유효성 오류"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 요청"),
            @ApiResponse(responseCode = "403", description = "기부단체 권한 없음")
    })
    @PatchMapping("/me/password")
    public ResponseEntity<Void> updateMyPassword(
            Authentication authentication,
            @RequestBody @Valid FoundationPasswordUpdateRequestDTO requestDTO) {
        Long foundationNo = (Long) authentication.getDetails();
        foundationService.updateFoundationPassword(foundationNo, requestDTO);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "내 캠페인 목록 조회", description = "로그인한 기부단체의 캠페인 목록을 최신순으로 페이징 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 요청"),
            @ApiResponse(responseCode = "403", description = "기부단체 권한 없음")
    })
    @GetMapping("/me/campaigns")
    public ResponseEntity<Page<CampaignListResponseDTO>> getMyCampaigns(
            Authentication authentication,
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Long foundationNo = (Long) authentication.getDetails();
        return ResponseEntity.ok(foundationService.getMyCampaigns(foundationNo, pageable));
    }

    @Operation(summary = "내 캠페인 목록 필터 조회", description = "로그인한 기부단체의 캠페인 목록을 캠페인 상태 필터와 키워드 검색으로 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 요청"),
            @ApiResponse(responseCode = "403", description = "기부단체 권한 없음")
    })
    @GetMapping("/me/campaigns/filter")
    public ResponseEntity<Page<FoundationMyCampaignDTO>> getMyCampaignsWithFilter(
            Authentication authentication,
            @Parameter(description = "캠페인 상태 필터 (PENDING, RECRUITING, ACTIVE, ENDED, SETTLED, COMPLETED, CANCELLED)", example = "ACTIVE")
            @RequestParam(required = false) CampaignStatus campaignStatus,
            @Parameter(description = "캠페인 제목 키워드 검색", example = "어린이")
            @RequestParam(defaultValue = "") String keyword,
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Long foundationNo = (Long) authentication.getDetails();
        return ResponseEntity.ok(foundationService.getMyCampaignsWithFilter(foundationNo, campaignStatus, keyword, pageable));
    }

    @Operation(summary = "마이페이지 통계 조회", description = "로그인한 기부단체의 진행 중 캠페인 수와 이번 달 모금액을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 요청"),
            @ApiResponse(responseCode = "403", description = "기부단체 권한 없음")
    })
    @GetMapping("/me/stats")
    public ResponseEntity<FoundationMyPageStatsDTO> getMyPageStats(Authentication authentication) {
        Long foundationNo = (Long) authentication.getDetails();
        return ResponseEntity.ok(foundationService.getMyPageStats(foundationNo));
    }

    @Operation(summary = "내 지갑 정보 조회", description = "로그인한 기부단체의 블록체인 지갑 주소와 잔액을 조회합니다. walletAddress가 null이면 지갑 미연동 상태입니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 요청"),
            @ApiResponse(responseCode = "403", description = "기부단체 권한 없음")
    })
    @GetMapping("/me/wallet")
    public ResponseEntity<FoundationWalletDTO> getMyWalletInfo(Authentication authentication) {
        Long foundationNo = (Long) authentication.getDetails();
        return ResponseEntity.ok(foundationService.getMyWalletInfo(foundationNo));
    }

    @Operation(summary = "내 정산 내역 조회", description = "로그인한 기부단체의 캠페인별 정산 내역을 최신순으로 페이징 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 요청"),
            @ApiResponse(responseCode = "403", description = "기부단체 권한 없음")
    })
    @GetMapping("/me/settlements")
    public ResponseEntity<Page<FoundationSettlementDTO>> getMySettlements(
            Authentication authentication,
            @PageableDefault(sort = "settledAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Long foundationNo = (Long) authentication.getDetails();
        return ResponseEntity.ok(foundationService.getMySettlements(foundationNo, pageable));
    }

    @Operation(summary = "내 환금(현금화) 내역 조회", description = "로그인한 기부단체의 토큰 환금(현금화) 내역을 최신순으로 페이징 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 요청"),
            @ApiResponse(responseCode = "403", description = "기부단체 권한 없음")
    })
    @GetMapping("/me/redemptions")
    public ResponseEntity<Page<FoundationRedemptionDTO>> getMyRedemptions(
            Authentication authentication,
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Long foundationNo = (Long) authentication.getDetails();
        return ResponseEntity.ok(foundationService.getMyRedemptions(foundationNo, pageable));
    }
}

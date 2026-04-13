package com.merge.final_project.org;

import com.merge.final_project.campaign.campaigns.dto.CampaignListResponseDTO;
import com.merge.final_project.org.dto.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    public ResponseEntity<Void> apply(@RequestPart("data") @Valid FoundationApplyRequestDTO requestDTO,
                                      @RequestPart(value = "profileImage", required = false) MultipartFile profileImage) {
        foundationService.apply(requestDTO, profileImage);
        return ResponseEntity.ok().build();
    }

    // 승인 완료된 기부단체 리스트 조회 (accountStatus 파라미터로 활성화/비활성화 필터링)
    @GetMapping("/all")
    public ResponseEntity<Page<FoundationListResponseDTO>> getApprovedList(
            @RequestParam(required = false) AccountStatus accountStatus,
            Pageable pageable) {
        return ResponseEntity.ok(foundationService.getApprovedFoundationList(accountStatus, pageable));
    }

    // 기부단체 상세 조회
    @GetMapping("/{foundationNo}")
    public ResponseEntity<FoundationDetailResponseDTO> getDetail(@PathVariable Long foundationNo) {
        return ResponseEntity.ok(foundationService.getFoundationDetail(foundationNo));
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

    //기부단체가 본인 캠페인 리스트 조회
    @GetMapping("/me/campaigns")
    public ResponseEntity<Page<CampaignListResponseDTO>> getMyCampaigns(
            Authentication authentication,
            Pageable pageable) {
        Long foundationNo = (Long) authentication.getDetails();
        return ResponseEntity.ok(foundationService.getMyCampaigns(foundationNo, pageable));
    }
}

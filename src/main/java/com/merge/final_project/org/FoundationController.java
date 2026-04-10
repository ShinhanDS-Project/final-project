package com.merge.final_project.org;

import com.merge.final_project.org.dto.FoundationApplyRequestDTO;
import com.merge.final_project.org.dto.FoundationDetailResponseDTO;
import com.merge.final_project.org.dto.FoundationListResponseDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
}

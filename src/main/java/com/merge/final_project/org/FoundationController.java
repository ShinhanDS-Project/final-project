package com.merge.final_project.org;

import com.merge.final_project.org.dto.FoundationApplyRequestDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/foundation")
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

    // 비회원 기부단체 가입 신청 -> 사진은 멀티파트로 받음.
    @PostMapping(value= "/apply", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> apply(@RequestPart("data") @Valid FoundationApplyRequestDTO requestDTO,
                                      @RequestPart(value="profileImage", required = false) MultipartFile profileImage) {
        foundationService.apply(requestDTO, profileImage);
        return ResponseEntity.ok().build();
    }

    //관리자가 승인 전 기부단체 리스트 불러오는 경로

    //관리자가 승인 후 기부단체 리스트 불러오는 경로

    //관리자가 승인 전 기부단체 상세 정보 조회하는 경로

    //관리자가 기부단체 review_status 상태 변환하는 경로

}

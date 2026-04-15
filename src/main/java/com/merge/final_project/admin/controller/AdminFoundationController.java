package com.merge.final_project.admin.controller;

import com.merge.final_project.org.AccountStatus;
import com.merge.final_project.org.FoundationService;
import com.merge.final_project.org.ReviewStatus;
import com.merge.final_project.org.dto.FoundationDetailResponseDTO;
import com.merge.final_project.org.dto.FoundationListResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/foundation")
@RequiredArgsConstructor
public class AdminFoundationController {

    private final FoundationService foundationService;

    // 관리자 기부단체 상세 조회 — 민감 정보 포함 (이메일, 계좌, 상태값 등 모두 노출)
    @GetMapping("/{foundationNo}")
    public ResponseEntity<FoundationDetailResponseDTO> getDetail(@PathVariable Long foundationNo) {
        return ResponseEntity.ok(foundationService.getFoundationDetail(foundationNo));
    }

    // 승인 전 기부단체 리스트 조회 — 키워드 검색 + 페이징 + 정렬
    @GetMapping("/applications")
    public ResponseEntity<Page<FoundationListResponseDTO>> getApplicationList(
            @RequestParam(required = false) ReviewStatus reviewStatus,
            @RequestParam(defaultValue = "") String keyword,
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(foundationService.getFoundationApplicationListWithFilter(reviewStatus, keyword, pageable));
    }

    // 반려된 기부단체 리스트 조회 — 키워드 검색 + 페이징 (기본: 최신순)
    @GetMapping("/rejected")
    public ResponseEntity<Page<FoundationListResponseDTO>> getRejectedList(
            @RequestParam(defaultValue = "") String keyword,
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(foundationService.getRejectedFoundationListWithFilter(keyword, pageable));
    }

    // [가빈] 승인된 기부단체 목록 조회 — 상태 필터 + 키워드 검색 + 페이징 (기본: 최신순)
    @GetMapping("/approved")
    public ResponseEntity<Page<FoundationListResponseDTO>> getApprovedList(
            @RequestParam(required = false) AccountStatus accountStatus,
            @RequestParam(defaultValue = "") String keyword,
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(foundationService.getApprovedFoundationListForAdmin(accountStatus, keyword, pageable));
    }

    // 기부단체 승인
    @PatchMapping("/{foundationNo}/approve")
    public ResponseEntity<Void> approve(@PathVariable Long foundationNo) {
        foundationService.approveFoundation(foundationNo);
        return ResponseEntity.ok().build();
    }

    // 기부단체 반려
    @PatchMapping("/{foundationNo}/reject")
    public ResponseEntity<Void> reject(@PathVariable Long foundationNo) {
        foundationService.rejectFoundationForIllegal(foundationNo);
        return ResponseEntity.ok().build();
    }

    // [가빈] 기부단체 활성화 (임시 비밀번호 발급 + 메일 발송)
    @PatchMapping("/{foundationNo}/activate")
    public ResponseEntity<Void> activate(@PathVariable Long foundationNo) {
        foundationService.activateFoundation(foundationNo);
        return ResponseEntity.ok().build();
    }

    // [가빈] 기부단체 비활성화 (비활성화 메일 발송)
    @PatchMapping("/{foundationNo}/deactivate")
    public ResponseEntity<Void> deactivate(@PathVariable Long foundationNo) {
        foundationService.deactivateFoundation(foundationNo);
        return ResponseEntity.ok().build();
    }
}

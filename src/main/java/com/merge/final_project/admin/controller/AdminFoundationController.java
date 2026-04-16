package com.merge.final_project.admin.controller;

import com.merge.final_project.org.AccountStatus;
import com.merge.final_project.org.FoundationService;
import com.merge.final_project.org.ReviewStatus;
import com.merge.final_project.org.dto.FoundationDetailResponseDTO;
import com.merge.final_project.org.dto.FoundationListResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "관리자 기부단체 관리", description = "관리자 기부단체 조회·승인·반려·활성화·비활성화 API")
@RestController
@RequestMapping("/admin/foundation")
@RequiredArgsConstructor
public class AdminFoundationController {

    private final FoundationService foundationService;

    @Operation(summary = "기부단체 상세 조회 (관리자)", description = "관리자용 기부단체 상세 정보를 조회합니다. 이메일·계좌·상태 등 민감 정보 포함.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "기부단체를 찾을 수 없음"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 요청"),
            @ApiResponse(responseCode = "403", description = "관리자 권한 없음")
    })
    @GetMapping("/{foundationNo}")
    public ResponseEntity<FoundationDetailResponseDTO> getDetail(
            @Parameter(description = "기부단체 번호", example = "1") @PathVariable Long foundationNo) {
        return ResponseEntity.ok(foundationService.getFoundationDetail(foundationNo));
    }

    // 승인 전 기부단체 리스트 조회 — 키워드 검색 + 페이징 + 정렬
    @Operation(summary = "가입 신청 대기 기부단체 목록 조회", description = "accountStatus = PRE_REGISTERED 인 기부단체 가입 신청 목록을 reviewStatus 필터, 키워드 검색, 정렬, 페이징으로 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 요청"),
            @ApiResponse(responseCode = "403", description = "관리자 권한 없음")
    })
    @GetMapping("/applications")
    public ResponseEntity<Page<FoundationListResponseDTO>> getApplicationList(
            @Parameter(description = "심사 상태 필터 (CLEAN, SIMILAR, ILLEGAL)", example = "CLEAN")
            @RequestParam(required = false) ReviewStatus reviewStatus,
            @Parameter(description = "단체명 또는 대표자명 키워드 검색", example = "초록")
            @RequestParam(defaultValue = "") String keyword,
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(foundationService.getFoundationApplicationListWithFilter(reviewStatus, keyword, pageable));
    }

    @Operation(summary = "반려된 기부단체 목록 조회", description = "반려(ILLEGAL/REJECTED) 상태의 기부단체 목록을 키워드 검색과 페이징으로 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 요청"),
            @ApiResponse(responseCode = "403", description = "관리자 권한 없음")
    })
    @GetMapping("/rejected")
    public ResponseEntity<Page<FoundationListResponseDTO>> getRejectedList(
            @Parameter(description = "단체명 또는 대표자명 키워드 검색", example = "초록")
            @RequestParam(defaultValue = "") String keyword,
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(foundationService.getRejectedFoundationListWithFilter(keyword, pageable));
    }

    @Operation(summary = "승인된 기부단체 목록 조회", description = "reviewStatus = APPROVED 인 기부단체 목록을 계정 상태 필터, 키워드 검색, 페이징으로 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 요청"),
            @ApiResponse(responseCode = "403", description = "관리자 권한 없음")
    })
    @GetMapping("/approved")
    public ResponseEntity<Page<FoundationListResponseDTO>> getApprovedList(
            @Parameter(description = "계정 상태 필터 (ACTIVE, INACTIVE) — 미입력 시 전체 조회", example = "ACTIVE")
            @RequestParam(required = false) AccountStatus accountStatus,
            @Parameter(description = "단체명 또는 대표자명 키워드 검색", example = "초록")
            @RequestParam(defaultValue = "") String keyword,
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(foundationService.getApprovedFoundationListForAdmin(accountStatus, keyword, pageable));
    }

    @Operation(summary = "기부단체 승인", description = "가입 신청한 기부단체를 승인합니다. 임시 비밀번호가 발급되어 이메일로 발송됩니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "승인 성공"),
            @ApiResponse(responseCode = "404", description = "기부단체를 찾을 수 없음"),
            @ApiResponse(responseCode = "409", description = "이미 처리된 기부단체"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 요청"),
            @ApiResponse(responseCode = "403", description = "관리자 권한 없음")
    })
    @PatchMapping("/{foundationNo}/approve")
    public ResponseEntity<Void> approve(
            @Parameter(description = "기부단체 번호", example = "1") @PathVariable Long foundationNo) {
        foundationService.approveFoundation(foundationNo);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "기부단체 반려", description = "가입 신청한 기부단체를 반려(불법단체 등)합니다. 계정 상태가 INACTIVE로 변경됩니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "반려 성공"),
            @ApiResponse(responseCode = "404", description = "기부단체를 찾을 수 없음"),
            @ApiResponse(responseCode = "409", description = "이미 처리된 기부단체"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 요청"),
            @ApiResponse(responseCode = "403", description = "관리자 권한 없음")
    })
    @PatchMapping("/{foundationNo}/reject")
    public ResponseEntity<Void> reject(
            @Parameter(description = "기부단체 번호", example = "1") @PathVariable Long foundationNo) {
        foundationService.rejectFoundationForIllegal(foundationNo);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "기부단체 활성화", description = "비활성화된 기부단체를 활성화합니다. 임시 비밀번호가 재발급되어 이메일로 발송됩니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "활성화 성공"),
            @ApiResponse(responseCode = "404", description = "기부단체를 찾을 수 없음"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 요청"),
            @ApiResponse(responseCode = "403", description = "관리자 권한 없음")
    })
    @PatchMapping("/{foundationNo}/activate")
    public ResponseEntity<Void> activate(@PathVariable Long foundationNo) {
        foundationService.activateFoundation(foundationNo);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "기부단체 비활성화", description = "활성화된 기부단체를 비활성화합니다. 비활성화 안내 이메일이 발송됩니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "비활성화 성공"),
            @ApiResponse(responseCode = "404", description = "기부단체를 찾을 수 없음"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 요청"),
            @ApiResponse(responseCode = "403", description = "관리자 권한 없음")
    })
    @PatchMapping("/{foundationNo}/deactivate")
    public ResponseEntity<Void> deactivate(
            @Parameter(description = "기부단체 번호", example = "1") @PathVariable Long foundationNo) {
        foundationService.deactivateFoundation(foundationNo);
        return ResponseEntity.ok().build();
    }
}

package com.merge.final_project.admin.controller;

import com.merge.final_project.admin.dto.AdminUserResponseDTO;
import com.merge.final_project.admin.service.AdminUserService;
import com.merge.final_project.user.users.UserStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "관리자 회원 관리", description = "관리자 회원 조회·활성화·비활성화 API")
@RestController
@RequestMapping("/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final AdminUserService adminUserService;

    @Operation(summary = "회원 활성화", description = "비활성화된 회원 계정을 활성화합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "활성화 성공"),
            @ApiResponse(responseCode = "404", description = "회원을 찾을 수 없음"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 요청"),
            @ApiResponse(responseCode = "403", description = "관리자 권한 없음")
    })
    @PatchMapping("/{userNo}/activate")
    public ResponseEntity<Void> activate(
            @Parameter(description = "회원 번호", example = "1") @PathVariable Long userNo) {
        adminUserService.activateUser(userNo);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "회원 비활성화", description = "활성화된 회원 계정을 비활성화합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "비활성화 성공"),
            @ApiResponse(responseCode = "404", description = "회원을 찾을 수 없음"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 요청"),
            @ApiResponse(responseCode = "403", description = "관리자 권한 없음")
    })
    @PatchMapping("/{userNo}/deactivate")
    public ResponseEntity<Void> deactivate(
            @Parameter(description = "회원 번호", example = "1") @PathVariable Long userNo) {
        adminUserService.deactivateUser(userNo);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "회원 목록 조회", description = "회원 목록을 계정 상태 필터, 키워드 검색, 페이징으로 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 요청"),
            @ApiResponse(responseCode = "403", description = "관리자 권한 없음")
    })
    @GetMapping
    public ResponseEntity<Page<AdminUserResponseDTO>> getUsers(
            @Parameter(description = "회원 상태 필터 (ACTIVE, INACTIVE)", example = "ACTIVE")
            @RequestParam(required = false) UserStatus status,
            @Parameter(description = "이름 또는 이메일 키워드 검색", example = "홍길동")
            @RequestParam(required = false) String keyword,
            Pageable pageable) {
        return ResponseEntity.ok(adminUserService.getUsers(status, keyword, pageable));
    }
}

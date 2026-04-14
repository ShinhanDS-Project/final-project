package com.merge.final_project.admin.adminlog;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@Tag(name = "관리자 활동 로그", description = "관리자 활동 로그 조회 API")
@RestController
@RequestMapping("/admin/logs")
@RequiredArgsConstructor
public class AdminLogController {

    private final AdminLogService adminLogService;

    @Operation(summary = "관리자 활동 로그 조회", description = "actionType, targetType, 날짜 범위, 키워드로 관리자 활동 로그를 필터링하여 페이징 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 요청"),
            @ApiResponse(responseCode = "403", description = "관리자 권한 없음")
    })
    @GetMapping
    public ResponseEntity<Page<AdminLogResponseDTO>> getLogs(
            @Parameter(description = "행동 유형 (APPROVE, REJECT, ACTIVATE, DEACTIVATE 등)", example = "APPROVE")
            @RequestParam(required = false) ActionType actionType,
            @Parameter(description = "대상 유형 (FOUNDATION, CAMPAIGN, REPORT, USER 등)", example = "FOUNDATION")
            @RequestParam(required = false) TargetType targetType,
            @Parameter(description = "조회 시작 일시 (ISO 8601 형식, 예: 2024-01-01T00:00:00)", example = "2024-01-01T00:00:00")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @Parameter(description = "조회 종료 일시 (ISO 8601 형식, 예: 2024-12-31T23:59:59)", example = "2024-12-31T23:59:59")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @Parameter(description = "설명 키워드 검색 (대소문자 무관)", example = "승인")
            @RequestParam(defaultValue = "") String keyword,
            Pageable pageable) {
        return ResponseEntity.ok(
                adminLogService.getLogsWithFilter(actionType, targetType, startDate, endDate, keyword, pageable)
        );
    }
}

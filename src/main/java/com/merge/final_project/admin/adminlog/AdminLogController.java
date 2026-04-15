package com.merge.final_project.admin.adminlog;

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

@RestController
@RequestMapping("/admin/logs")
@RequiredArgsConstructor
public class AdminLogController {

    private final AdminLogService adminLogService;

    // 관리자 활동 로그 조회 (actionType, targetType, 날짜 범위, 키워드(설명) 필터링)
    @GetMapping
    public ResponseEntity<Page<AdminLogResponseDTO>> getLogs(
            @RequestParam(required = false) ActionType actionType,
            @RequestParam(required = false) TargetType targetType,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(defaultValue = "") String keyword,
            Pageable pageable) {
        return ResponseEntity.ok(
                adminLogService.getLogsWithFilter(actionType, targetType, startDate, endDate, keyword, pageable)
        );
    }
}

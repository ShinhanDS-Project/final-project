package com.merge.final_project.admin.controller;

import com.merge.final_project.notification.email.EmailTemplateType;
import com.merge.final_project.notification.email.history.EmailSendListRepository;
import com.merge.final_project.notification.email.history.EmailSendListResponseDTO;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "관리자 메일 발송 내역", description = "관리자 이메일 발송 내역 조회 API")
@RestController
@RequestMapping("/admin/email-send-list")
@RequiredArgsConstructor
public class AdminEmailSendListController {

    private final EmailSendListRepository emailSendListRepository;

    @Operation(summary = "메일 발송 내역 조회", description = "이메일 발송 내역을 템플릿 유형 필터와 페이징으로 조회합니다. 기본: 최신순 20건.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 요청"),
            @ApiResponse(responseCode = "403", description = "관리자 권한 없음")
    })
    @GetMapping
    public ResponseEntity<Page<EmailSendListResponseDTO>> getEmailSendList(
            @Parameter(description = "이메일 템플릿 유형 필터 (ACCOUNT_APPROVED, ACCOUNT_REJECTED, FOUNDATION_INACTIVE_BATCH, FOUNDATION_DEACTIVATED_BY_ADMIN 등)", example = "ACCOUNT_APPROVED")
            @RequestParam(required = false) EmailTemplateType templateType,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        if (templateType != null) {
            return ResponseEntity.ok(
                    emailSendListRepository.findByTemplateType(templateType, pageable)
                            .map(EmailSendListResponseDTO::from));
        }
        return ResponseEntity.ok(
                emailSendListRepository.findAll(pageable)
                        .map(EmailSendListResponseDTO::from));
    }
}

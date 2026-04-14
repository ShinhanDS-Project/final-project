package com.merge.final_project.admin.controller;

import com.merge.final_project.notification.email.EmailTemplateType;
import com.merge.final_project.notification.email.history.EmailSendListRepository;
import com.merge.final_project.notification.email.history.EmailSendListResponseDTO;
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

@RestController
@RequestMapping("/admin/email-send-list")
@RequiredArgsConstructor
public class AdminEmailSendListController {

    private final EmailSendListRepository emailSendListRepository;

    // 메일 발송 내역 목록 조회 (templateType 필터 선택 가능, 기본 최신순)
    @GetMapping
    public ResponseEntity<Page<EmailSendListResponseDTO>> getEmailSendList(
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

package com.merge.final_project.admin.controller;

import com.merge.final_project.recipient.beneficiary.dto.BeneficiaryInfoResponseDTO;
import com.merge.final_project.recipient.beneficiary.service.BeneficiaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/beneficiary")
@RequiredArgsConstructor
public class AdminBeneficiaryController {

    private final BeneficiaryService beneficiaryService;

    // [가빈] 관리자 수혜자 상세 조회
    @GetMapping("/{no}")
    public ResponseEntity<BeneficiaryInfoResponseDTO> getBeneficiary(@PathVariable Long no) {
        return ResponseEntity.ok(beneficiaryService.getBeneficiaryById(no));
    }
}

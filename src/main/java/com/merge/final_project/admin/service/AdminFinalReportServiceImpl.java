package com.merge.final_project.admin.service;

import com.merge.final_project.admin.Admin;
import com.merge.final_project.admin.AdminRepository;
import com.merge.final_project.admin.adminlog.ActionType;
import com.merge.final_project.admin.adminlog.AdminLogService;
import com.merge.final_project.admin.adminlog.TargetType;
import com.merge.final_project.global.ImageRepository;
import com.merge.final_project.global.exceptions.BusinessException;
import com.merge.final_project.global.exceptions.ErrorCode;
import com.merge.final_project.notification.inapp.NotificationService;
import com.merge.final_project.notification.inapp.NotificationType;
import com.merge.final_project.notification.inapp.RecipientType;
import com.merge.final_project.report.finalreport.ReportApprovalStatus;
import com.merge.final_project.report.finalreport.dto.FinalReportResponseDTO;
import com.merge.final_project.report.finalreport.entitiy.FinalReport;
import com.merge.final_project.report.finalreport.repository.FinalReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class AdminFinalReportServiceImpl implements AdminFinalReportService {

    private final FinalReportRepository finalReportRepository;
    private final AdminRepository adminRepository;
    private final AdminLogService adminLogService;
    private final NotificationService notificationService;
    private final ImageRepository imageRepository;

    // 승인 대기 중인 활동 보고서 목록 조회하는 기능.
    @Override
    public Page<FinalReportResponseDTO> getPendingReports(Pageable pageable) {
        return finalReportRepository.findByApprovalStatus(ReportApprovalStatus.PENDING, pageable)
                .map(report -> {
                    var images = imageRepository.findByTargetNameAndTargetNo("final_report", report.getReportNo());
                    return new FinalReportResponseDTO(report, images);
                });
    }

    //보고서 승인 -> 로그 남기고, 알림 보내는 과정 중 오류 생기면 롤백 해야함. 따라서 트랜잭션 적용
    @Override
    @Transactional
    public void approveReport(Long reportNo) {
        //보고서 아이디로 해당 보고서 먼저 찾아오고.
        FinalReport report = finalReportRepository.findById(reportNo)
                .orElseThrow(() -> new BusinessException(ErrorCode.FINAL_REPORT_NOT_FOUND));

        //이미 승인된 상태면 중복 승인을 방지하기 위해 에러처리.
        if (report.getApprovalStatus() == ReportApprovalStatus.APPROVED) {
            throw new BusinessException(ErrorCode.FINAL_REPORT_ALREADY_PROCESSED);
        }

        report.changeStatus(ReportApprovalStatus.APPROVED, null);

        //현재 활동 보고서 승인한 관리자의 이력도 기록해야 함.
        Admin admin = getAdmin();
        adminLogService.log(ActionType.APPROVE, TargetType.FINAL_REPORT, reportNo,
                "[" + report.getTitle() + "] 활동 보고서 승인", admin);

        //보고서를 작성한 수혜자에게 알람을 전달해야 함.
        notificationService.send(RecipientType.BENEFICIARY, report.getBeneficiary_no(),
                NotificationType.FINAL_REPORT_APPROVED, "[" + report.getTitle() + "] 활동 보고서가 승인되었습니다.");
    }

    //반려 메서드 -> 승인과 유사
    @Override
    @Transactional
    public void rejectReport(Long reportNo, String reason) {
        FinalReport report = finalReportRepository.findById(reportNo)
                .orElseThrow(() -> new BusinessException(ErrorCode.FINAL_REPORT_NOT_FOUND));

        if (report.getApprovalStatus() == ReportApprovalStatus.REJECTED) {
            throw new BusinessException(ErrorCode.FINAL_REPORT_ALREADY_PROCESSED);
        }

        report.changeStatus(ReportApprovalStatus.REJECTED, reason);

        Admin admin = getAdmin();
        adminLogService.log(ActionType.REJECT, TargetType.FINAL_REPORT, reportNo,
                "[" + report.getTitle() + "] 활동 보고서 반려: " + reason, admin);

        notificationService.send(RecipientType.BENEFICIARY, report.getBeneficiary_no(),
                NotificationType.FINAL_REPORT_REJECTED, "[" + report.getTitle() + "] 활동 보고서가 반려되었습니다. 사유: " + reason);
    }

    //현재 로그인 되어 있는 관리자 객체를 가져오기 위한 메서드
    private Admin getAdmin() {
        String adminId = Objects.requireNonNull(
                SecurityContextHolder.getContext().getAuthentication()).getName();
        return adminRepository.findByAdminId(adminId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ADMIN_NOT_FOUND));
    }
}

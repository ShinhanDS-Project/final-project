package com.merge.final_project.admin;

import com.merge.final_project.admin.adminlog.ActionType;
import com.merge.final_project.admin.adminlog.AdminLogService;
import com.merge.final_project.admin.adminlog.TargetType;
import com.merge.final_project.admin.service.AdminFinalReportServiceImpl;
import com.merge.final_project.global.Image;
import com.merge.final_project.global.ImageRepository;
import com.merge.final_project.global.exceptions.BusinessException;
import com.merge.final_project.global.exceptions.ErrorCode;
import com.merge.final_project.campaign.campaigns.CampaignStatus;
import com.merge.final_project.campaign.campaigns.entity.Campaign;
import com.merge.final_project.campaign.campaigns.repository.CampaignRepository;
import com.merge.final_project.campaign.campaigns.CampaignStatus;
import com.merge.final_project.campaign.campaigns.entity.Campaign;
import com.merge.final_project.campaign.campaigns.repository.CampaignRepository;
import com.merge.final_project.notification.inapp.NotificationService;
import com.merge.final_project.notification.inapp.NotificationType;
import com.merge.final_project.notification.inapp.RecipientType;
import com.merge.final_project.report.finalreport.ReportApprovalStatus;
import com.merge.final_project.report.finalreport.dto.FinalReportResponseDTO;
import com.merge.final_project.report.finalreport.entitiy.FinalReport;
import com.merge.final_project.report.finalreport.repository.FinalReportRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminFinalReportServiceTest {

    @InjectMocks
    private AdminFinalReportServiceImpl adminFinalReportService;

    @Mock
    private FinalReportRepository finalReportRepository;

    @Mock
    private AdminRepository adminRepository;

    @Mock
    private AdminLogService adminLogService;

    @Mock
    private NotificationService notificationService;

    @Mock
    private ImageRepository imageRepository;

    @Mock
    private CampaignRepository campaignRepository;

    private Admin admin;

    @BeforeEach
    void setUp() {
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                "test-admin", null, List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );
        SecurityContextHolder.getContext().setAuthentication(auth);

        admin = Admin.builder()
                .adminId("test-admin")
                .name("테스트관리자")
                .build();
    }

    // ===================== 승인 테스트 =====================

    @Test
    @DisplayName("PENDING 상태 보고서 승인 시 상태가 APPROVED로 변경되고 캠페인이 COMPLETED가 되며 로그와 알림이 발송된다")
    void 보고서_승인_성공() {
        FinalReport report = FinalReport.builder()
                .reportNo(1L)
                .title("테스트 보고서")
                .beneficiary_no(20L)
                .campaign_no(10L)
                .approvalStatus(ReportApprovalStatus.PENDING)
                .build();

        Campaign campaign = mock(Campaign.class);
        when(campaign.getCampaignStatus()).thenReturn(CampaignStatus.SETTLED);

        when(finalReportRepository.findById(1L)).thenReturn(Optional.of(report));
        when(campaignRepository.findByCampaignNo(10L)).thenReturn(Optional.of(campaign));
        when(adminRepository.findByAdminId("test-admin")).thenReturn(Optional.of(admin));

        adminFinalReportService.approveReport(1L);

        assertThat(report.getApprovalStatus()).isEqualTo(ReportApprovalStatus.APPROVED);
        assertThat(report.getRejectReason()).isNull();
        verify(campaign).complete();
        verify(adminLogService).log(eq(ActionType.APPROVE), eq(TargetType.FINAL_REPORT), eq(1L), anyString(), eq(admin));
        verify(notificationService).send(eq(RecipientType.BENEFICIARY), eq(20L), eq(NotificationType.FINAL_REPORT_APPROVED), anyString());
    }

    @Test
    @DisplayName("존재하지 않는 보고서 승인 시 FINAL_REPORT_NOT_FOUND 예외가 발생한다")
    void 존재하지않는_보고서_승인_예외() {
        when(finalReportRepository.findById(999L)).thenReturn(Optional.empty());

        BusinessException exception = assertThrows(BusinessException.class,
                () -> adminFinalReportService.approveReport(999L));

        assertThat(exception.getMessage()).isEqualTo(ErrorCode.FINAL_REPORT_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("이미 승인된 보고서 재승인 시 FINAL_REPORT_ALREADY_PROCESSED 예외가 발생한다")
    void 이미_승인된_보고서_재승인_예외() {
        FinalReport report = FinalReport.builder()
                .reportNo(1L)
                .title("테스트 보고서")
                .approvalStatus(ReportApprovalStatus.APPROVED)
                .build();

        when(finalReportRepository.findById(1L)).thenReturn(Optional.of(report));

        BusinessException exception = assertThrows(BusinessException.class,
                () -> adminFinalReportService.approveReport(1L));

        assertThat(exception.getMessage()).isEqualTo(ErrorCode.FINAL_REPORT_ALREADY_PROCESSED.getMessage());
    }

    @Test
    @DisplayName("반려된 보고서는 재승인이 가능하다")
    void 반려된_보고서_재승인_성공() {
        FinalReport report = FinalReport.builder()
                .reportNo(1L)
                .title("테스트 보고서")
                .beneficiary_no(20L)
                .campaign_no(10L)
                .approvalStatus(ReportApprovalStatus.REJECTED)
                .build();

        Campaign campaign = mock(Campaign.class);
        when(campaign.getCampaignStatus()).thenReturn(CampaignStatus.SETTLED);

        when(finalReportRepository.findById(1L)).thenReturn(Optional.of(report));
        when(campaignRepository.findByCampaignNo(10L)).thenReturn(Optional.of(campaign));
        when(adminRepository.findByAdminId("test-admin")).thenReturn(Optional.of(admin));

        adminFinalReportService.approveReport(1L);

        assertThat(report.getApprovalStatus()).isEqualTo(ReportApprovalStatus.APPROVED);
        verify(campaign).complete();
        verify(adminLogService).log(eq(ActionType.APPROVE), eq(TargetType.FINAL_REPORT), eq(1L), anyString(), eq(admin));
    }

    @Test
    @DisplayName("보고서 승인 시 연결된 캠페인이 없으면 CAMPAIGN_NOT_FOUND 예외가 발생한다")
    void 보고서_승인_캠페인_없음_예외() {
        FinalReport report = FinalReport.builder()
                .reportNo(1L)
                .title("테스트 보고서")
                .beneficiary_no(20L)
                .campaign_no(999L)
                .approvalStatus(ReportApprovalStatus.PENDING)
                .build();

        when(finalReportRepository.findById(1L)).thenReturn(Optional.of(report));
        when(campaignRepository.findByCampaignNo(999L)).thenReturn(Optional.empty());

        BusinessException exception = assertThrows(BusinessException.class,
                () -> adminFinalReportService.approveReport(1L));

        assertThat(exception.getMessage()).isEqualTo(ErrorCode.CAMPAIGN_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("보고서 승인 시 캠페인이 이미 COMPLETED 상태면 CAMPAIGN_ALREADY_PROCESSED 예외가 발생한다")
    void 보고서_승인_캠페인_이미완료_예외() {
        FinalReport report = FinalReport.builder()
                .reportNo(1L)
                .title("테스트 보고서")
                .beneficiary_no(20L)
                .campaign_no(10L)
                .approvalStatus(ReportApprovalStatus.PENDING)
                .build();

        Campaign campaign = mock(Campaign.class);
        when(campaign.getCampaignStatus()).thenReturn(CampaignStatus.COMPLETED);

        when(finalReportRepository.findById(1L)).thenReturn(Optional.of(report));
        when(campaignRepository.findByCampaignNo(10L)).thenReturn(Optional.of(campaign));

        BusinessException exception = assertThrows(BusinessException.class,
                () -> adminFinalReportService.approveReport(1L));

        assertThat(exception.getMessage()).isEqualTo(ErrorCode.CAMPAIGN_ALREADY_PROCESSED.getMessage());
    }

    // ===================== 반려 테스트 =====================

    @Test
    @DisplayName("PENDING 상태 보고서 반려 시 상태가 REJECTED로 변경되고 사유가 저장되며 로그와 알림이 발송된다")
    void 보고서_반려_성공() {
        FinalReport report = FinalReport.builder()
                .reportNo(1L)
                .title("테스트 보고서")
                .beneficiary_no(20L)
                .approvalStatus(ReportApprovalStatus.PENDING)
                .build();

        when(finalReportRepository.findById(1L)).thenReturn(Optional.of(report));
        when(adminRepository.findByAdminId("test-admin")).thenReturn(Optional.of(admin));

        adminFinalReportService.rejectReport(1L, "내용 미흡");

        assertThat(report.getApprovalStatus()).isEqualTo(ReportApprovalStatus.REJECTED);
        assertThat(report.getRejectReason()).isEqualTo("내용 미흡");

        verify(adminLogService).log(eq(ActionType.REJECT), eq(TargetType.FINAL_REPORT), eq(1L), anyString(), eq(admin));
        verify(notificationService).send(eq(RecipientType.BENEFICIARY), eq(20L), eq(NotificationType.FINAL_REPORT_REJECTED), anyString());
    }

    @Test
    @DisplayName("존재하지 않는 보고서 반려 시 FINAL_REPORT_NOT_FOUND 예외가 발생한다")
    void 존재하지않는_보고서_반려_예외() {
        when(finalReportRepository.findById(999L)).thenReturn(Optional.empty());

        BusinessException exception = assertThrows(BusinessException.class,
                () -> adminFinalReportService.rejectReport(999L, "사유"));

        assertThat(exception.getMessage()).isEqualTo(ErrorCode.FINAL_REPORT_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("이미 반려된 보고서 재반려 시 FINAL_REPORT_ALREADY_PROCESSED 예외가 발생한다")
    void 이미_반려된_보고서_재반려_예외() {
        FinalReport report = FinalReport.builder()
                .reportNo(1L)
                .title("테스트 보고서")
                .approvalStatus(ReportApprovalStatus.REJECTED)
                .build();

        when(finalReportRepository.findById(1L)).thenReturn(Optional.of(report));

        BusinessException exception = assertThrows(BusinessException.class,
                () -> adminFinalReportService.rejectReport(1L, "사유"));

        assertThat(exception.getMessage()).isEqualTo(ErrorCode.FINAL_REPORT_ALREADY_PROCESSED.getMessage());
    }

    @Test
    @DisplayName("승인된 보고서도 문제 발생 시 반려할 수 있다")
    void 승인된_보고서_반려_성공() {
        FinalReport report = FinalReport.builder()
                .reportNo(1L)
                .title("테스트 보고서")
                .beneficiary_no(20L)
                .approvalStatus(ReportApprovalStatus.APPROVED)
                .build();

        when(finalReportRepository.findById(1L)).thenReturn(Optional.of(report));
        when(adminRepository.findByAdminId("test-admin")).thenReturn(Optional.of(admin));

        adminFinalReportService.rejectReport(1L, "내용 문제");

        assertThat(report.getApprovalStatus()).isEqualTo(ReportApprovalStatus.REJECTED);
        assertThat(report.getRejectReason()).isEqualTo("내용 문제");
        verify(adminLogService).log(eq(ActionType.REJECT), eq(TargetType.FINAL_REPORT), eq(1L), anyString(), eq(admin));
    }

    // ===================== 목록 조회 테스트 =====================

    @Test
    @DisplayName("승인 대기 보고서 목록 조회 시 PENDING 상태의 보고서만 반환된다")
    void 승인대기_보고서_목록_조회() {
        FinalReport report = FinalReport.builder()
                .reportNo(1L)
                .title("테스트 보고서")
                .beneficiary_no(20L)
                .approvalStatus(ReportApprovalStatus.PENDING)
                .build();

        Pageable pageable = PageRequest.of(0, 10);
        Page<FinalReport> reportPage = new PageImpl<>(List.of(report));

        when(finalReportRepository.findByApprovalStatus(ReportApprovalStatus.PENDING, pageable))
                .thenReturn(reportPage);
        when(imageRepository.findByTargetNameAndTargetNo(eq("final_report"), eq(1L)))
                .thenReturn(List.of());

        Page<FinalReportResponseDTO> result = adminFinalReportService.getPendingReports(pageable);

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getReportNo()).isEqualTo(1L);
        assertThat(result.getContent().get(0).getApprovalStatus()).isEqualTo(ReportApprovalStatus.PENDING);
    }
}

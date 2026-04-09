package com.merge.final_project.campaign;

import com.merge.final_project.admin.Admin;
import com.merge.final_project.admin.AdminRepository;
import com.merge.final_project.admin.adminlog.ActionType;
import com.merge.final_project.admin.adminlog.AdminLogService;
import com.merge.final_project.admin.adminlog.TargetType;
import com.merge.final_project.admin.service.AdminCampaignServiceImpl;
import com.merge.final_project.campaign.campaigns.ApprovalStatus;
import com.merge.final_project.campaign.campaigns.CampaignStatus;
import com.merge.final_project.campaign.campaigns.entity.Campaign;
import com.merge.final_project.campaign.campaigns.repository.CampaignRepository;
import com.merge.final_project.global.exceptions.BusinessException;
import com.merge.final_project.global.exceptions.ErrorCode;
import com.merge.final_project.notification.inapp.NotificationService;
import com.merge.final_project.notification.inapp.NotificationType;
import com.merge.final_project.notification.inapp.RecipientType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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
class AdminCampaignServiceTests {

    @InjectMocks
    private AdminCampaignServiceImpl adminCampaignService;

    @Mock
    private CampaignRepository campaignRepository;

    @Mock
    private AdminRepository adminRepository;

    @Mock
    private AdminLogService adminLogService;

    @Mock
    private NotificationService notificationService;

    private Admin admin;

    @BeforeEach
    void setUp() {
        // SecurityContext에 관리자 인증 정보 세팅 (adminId = "test-admin")
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
    @DisplayName("PENDING 상태 캠페인 승인 시 상태가 APPROVED/RECRUITING으로 변경되고 로그와 알림이 발송된다")
    void 캠페인_승인_성공() {
        Campaign campaign = Campaign.builder()
                .campaignNo(1L)
                .title("테스트 캠페인")
                .foundationNo(10L)
                .approvalStatus(ApprovalStatus.PENDING)
                .campaignStatus(CampaignStatus.PENDING)
                .build();

        when(campaignRepository.findById(1L)).thenReturn(Optional.of(campaign));
        when(adminRepository.findByAdminId("test-admin")).thenReturn(Optional.of(admin));

        adminCampaignService.approveCampaign(1L);

        // 승인 후 상태 검증
        assertThat(campaign.getApprovalStatus()).isEqualTo(ApprovalStatus.APPROVED);
        assertThat(campaign.getCampaignStatus()).isEqualTo(CampaignStatus.RECRUITING);
        assertThat(campaign.getApprovedAt()).isNotNull();

        // 관리자 로그 기록 검증
        verify(adminLogService).log(eq(ActionType.APPROVE), eq(TargetType.CAMPAIGN), eq(1L), anyString(), eq(admin));

        // 기부단체 알림 발송 검증
        verify(notificationService).send(eq(RecipientType.FOUNDATION), eq(10L), eq(NotificationType.CAMPAIGN_APPROVED), anyString());
    }

    @Test
    @DisplayName("존재하지 않는 캠페인 승인 시 CAMPAIGN_NOT_FOUND 예외가 발생한다")
    void 존재하지않는_캠페인_승인_예외() {
        when(campaignRepository.findById(999L)).thenReturn(Optional.empty());

        BusinessException exception = assertThrows(BusinessException.class,
                () -> adminCampaignService.approveCampaign(999L));

        assertThat(exception.getMessage()).isEqualTo(ErrorCode.CAMPAIGN_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("이미 승인된 캠페인 재승인 시 CAMPAIGN_ALREADY_PROCESSED 예외가 발생한다")
    void 이미_승인된_캠페인_재승인_예외() {
        // 이미 APPROVED 상태인 캠페인
        Campaign campaign = Campaign.builder()
                .campaignNo(1L)
                .title("테스트 캠페인")
                .approvalStatus(ApprovalStatus.APPROVED)
                .build();

        when(campaignRepository.findById(1L)).thenReturn(Optional.of(campaign));

        BusinessException exception = assertThrows(BusinessException.class,
                () -> adminCampaignService.approveCampaign(1L));

        assertThat(exception.getMessage()).isEqualTo(ErrorCode.CAMPAIGN_ALREADY_PROCESSED.getMessage());
    }

    @Test
    @DisplayName("이미 반려된 캠페인 승인 시 CAMPAIGN_ALREADY_PROCESSED 예외가 발생한다")
    void 이미_반려된_캠페인_승인_예외() {
        Campaign campaign = Campaign.builder()
                .campaignNo(1L)
                .title("테스트 캠페인")
                .approvalStatus(ApprovalStatus.REJECTED)
                .build();

        when(campaignRepository.findById(1L)).thenReturn(Optional.of(campaign));

        BusinessException exception = assertThrows(BusinessException.class,
                () -> adminCampaignService.approveCampaign(1L));

        assertThat(exception.getMessage()).isEqualTo(ErrorCode.CAMPAIGN_ALREADY_PROCESSED.getMessage());
    }

    // ===================== 반려 테스트 =====================

    @Test
    @DisplayName("PENDING 상태 캠페인 반려 시 상태가 REJECTED로 변경되고 사유가 저장되며 로그와 알림이 발송된다")
    void 캠페인_반려_성공() {
        Campaign campaign = Campaign.builder()
                .campaignNo(1L)
                .title("테스트 캠페인")
                .foundationNo(10L)
                .approvalStatus(ApprovalStatus.PENDING)
                .build();

        when(campaignRepository.findById(1L)).thenReturn(Optional.of(campaign));
        when(adminRepository.findByAdminId("test-admin")).thenReturn(Optional.of(admin));

        adminCampaignService.rejectCampaign(1L, "내용 미흡");

        // 반려 후 상태 및 사유 검증
        assertThat(campaign.getApprovalStatus()).isEqualTo(ApprovalStatus.REJECTED);
        assertThat(campaign.getRejectReason()).isEqualTo("내용 미흡");

        // 관리자 로그 기록 검증
        verify(adminLogService).log(eq(ActionType.REJECT), eq(TargetType.CAMPAIGN), eq(1L), anyString(), eq(admin));

        // 기부단체 알림 발송 검증
        verify(notificationService).send(eq(RecipientType.FOUNDATION), eq(10L), eq(NotificationType.CAMPAIGN_REJECTED), anyString());
    }

    @Test
    @DisplayName("존재하지 않는 캠페인 반려 시 CAMPAIGN_NOT_FOUND 예외가 발생한다")
    void 존재하지않는_캠페인_반려_예외() {
        when(campaignRepository.findById(999L)).thenReturn(Optional.empty());

        BusinessException exception = assertThrows(BusinessException.class,
                () -> adminCampaignService.rejectCampaign(999L, "사유"));

        assertThat(exception.getMessage()).isEqualTo(ErrorCode.CAMPAIGN_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("이미 처리된 캠페인 반려 시 CAMPAIGN_ALREADY_PROCESSED 예외가 발생한다")
    void 이미_처리된_캠페인_반려_예외() {
        // 이미 APPROVED 상태인 캠페인
        Campaign campaign = Campaign.builder()
                .campaignNo(1L)
                .title("테스트 캠페인")
                .approvalStatus(ApprovalStatus.APPROVED)
                .build();

        when(campaignRepository.findById(1L)).thenReturn(Optional.of(campaign));

        BusinessException exception = assertThrows(BusinessException.class,
                () -> adminCampaignService.rejectCampaign(1L, "사유"));

        assertThat(exception.getMessage()).isEqualTo(ErrorCode.CAMPAIGN_ALREADY_PROCESSED.getMessage());
    }
}
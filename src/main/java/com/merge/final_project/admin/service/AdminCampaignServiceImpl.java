package com.merge.final_project.admin.service;

import com.merge.final_project.admin.Admin;
import com.merge.final_project.admin.AdminRepository;
import com.merge.final_project.admin.adminlog.ActionType;
import com.merge.final_project.admin.adminlog.AdminLogService;
import com.merge.final_project.admin.adminlog.TargetType;
import com.merge.final_project.campaign.campaigns.ApprovalStatus;
import com.merge.final_project.campaign.campaigns.dto.CampaignListResponseDTO;
import com.merge.final_project.campaign.campaigns.entity.Campaign;
import com.merge.final_project.campaign.campaigns.repository.CampaignRepository;
import com.merge.final_project.global.exceptions.BusinessException;
import com.merge.final_project.global.exceptions.ErrorCode;
import com.merge.final_project.notification.inapp.NotificationService;
import com.merge.final_project.notification.inapp.NotificationType;
import com.merge.final_project.notification.inapp.RecipientType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class AdminCampaignServiceImpl implements AdminCampaignService {

    private final CampaignRepository campaignRepository;
    private final AdminRepository adminRepository;
    private final AdminLogService adminLogService;
    private final NotificationService notificationService;

    //캠페인 승인 메서드 -> 승인하면 관리자 활동 로그에 추가하고 기부단체에게 알림 보냄.
    @Override
    @Transactional
    public void approveCampaign(Long campaignNo) {
        Campaign campaign = campaignRepository.findById(campaignNo)
                .orElseThrow(() -> new BusinessException(ErrorCode.CAMPAIGN_NOT_FOUND));

        // 이미 승인된 캠페인은 재승인 불가
        if (campaign.getApprovalStatus() == ApprovalStatus.APPROVED) {
            throw new BusinessException(ErrorCode.CAMPAIGN_ALREADY_PROCESSED);
        }

        campaign.approve();

        // 관리자 로그
        Admin admin = getAdmin();
        adminLogService.log(ActionType.APPROVE, TargetType.CAMPAIGN, campaignNo,
                campaign.getTitle() + " 캠페인 승인", admin);

        // 기부단체에게 알림
        notificationService.send(RecipientType.FOUNDATION, campaign.getFoundationNo(),
                NotificationType.CAMPAIGN_APPROVED, "[" + campaign.getTitle() + "] 캠페인이 승인되었습니다.");
    }

    //캠페인 반려 -> 관리자 로그 남기고 기부단체에게 알림 및 사유 전달.
    @Override
    @Transactional
    public void rejectCampaign(Long campaignNo, String reason) {
        Campaign campaign = campaignRepository.findById(campaignNo)
                .orElseThrow(() -> new BusinessException(ErrorCode.CAMPAIGN_NOT_FOUND));

        // 이미 반려된 캠페인은 재반려 불가
        if (campaign.getApprovalStatus() == ApprovalStatus.REJECTED) {
            throw new BusinessException(ErrorCode.CAMPAIGN_ALREADY_PROCESSED);
        }

        campaign.reject(reason);

        // 관리자 로그
        Admin admin = getAdmin();
        adminLogService.log(ActionType.REJECT, TargetType.CAMPAIGN, campaignNo,
                campaign.getTitle() + " 캠페인 반려: " + reason, admin);

        // 기부단체에게 알림
        notificationService.send(RecipientType.FOUNDATION, campaign.getFoundationNo(),
                NotificationType.CAMPAIGN_REJECTED, "[" + campaign.getTitle() + "] 캠페인이 반려되었습니다. 사유: " + reason);
    }

    // 보류 상태인 캠페인 리스트 조회.
    @Override
    public Page<CampaignListResponseDTO> getPendingCampaigns(Pageable pageable) {
        return campaignRepository.findByApprovalStatus(ApprovalStatus.PENDING, pageable)
                .map(CampaignListResponseDTO::from);
    }

    //반려 상태인 캠페인 리스트 조회
    @Override
    public Page<CampaignListResponseDTO> getRejectedCampaigns(Pageable pageable) {
        return campaignRepository.findByApprovalStatus(ApprovalStatus.REJECTED, pageable)
                .map(CampaignListResponseDTO::from);
    }

    //승인 상태인 캠페인 리스트 조회 => 진행 중인 상태라서 RECRUITING 찾아와야 함
    @Override
    public Page<CampaignListResponseDTO> getApprovedCampaigns(Pageable pageable) {
        return campaignRepository.findByApprovalStatus(ApprovalStatus.APPROVED, pageable)
                .map(CampaignListResponseDTO::from);
    }

    //로그인한 사람의 토큰에서 어떤 adminId인지 추출해서 adminlog의 admin객체로 넘기는 메서드
    private Admin getAdmin() {
        String adminId = Objects.requireNonNull(SecurityContextHolder.getContext().getAuthentication()).getName();
        return adminRepository.findByAdminId(adminId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ADMIN_NOT_FOUND));
    }
}